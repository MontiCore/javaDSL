package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.context.AdaptationContextFactory;
import de.monticore.codeAdaption.context.ConcretizationService;
import de.monticore.codeAdaption.context.GroupingMappingService;
import de.monticore.codeAdaption.context.MappingConformanceService;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.handler.multiIncarnation.*;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdaterFactory;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.apache.commons.io.FileUtils;

public class CodeAdapter {
  protected Set<AdapterParam> adapterParams;
  protected Set<CDConfParameter> confParams;

  public CodeAdapter(Set<AdapterParam> adapterParams, Set<CDConfParameter> confParams) {
    this.adapterParams = adapterParams;
    this.confParams = confParams;
  }

  /**
   * Adapts reference Java code to a concrete class diagram without completing the concrete model
   * first.
   *
   * @param referenceCD reference class diagram
   * @param concreteCD concrete class diagram
   * @param mappings mapping stereotype names to apply
   * @param refHwcPath reference handwritten-code directory
   * @param conHwcPath concrete handwritten-code directory
   * @param outputPath output directory
   */
  public void adapt(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath) {
    adapt(referenceCD, concreteCD, mappings, refHwcPath, conHwcPath, outputPath, false, true);
  }

  /**
   * Adapts reference Java code using caller-provided updater instances.
   *
   * <p>The factory is called once for every isolated temporary adaptation run and must return a
   * fresh updater each time.
   */
  public void adapt(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath,
      CodeUpdaterFactory updaterFactory) {
    adapt(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        conHwcPath,
        outputPath,
        false,
        true,
        updaterFactory);
  }

  /**
   * Adapts reference Java code to a concrete class diagram.
   *
   * <p>With {@code useConcretization=true}, cdconcretization may complete the concrete CD before
   * adaptation. With {@code useConcretization=false}, incarnation contexts are derived manually from
   * stereotypes and deterministic name rules, conflicts are reported before the output directory is
   * cleaned, and the concrete CD is not changed.
   *
   * <p>{@code useCommonParentForMultipleIncarnations} permits manual adaptation to resolve
   * class/interface mismatches and multi-incarnation type targets through an available common
   * parent or implemented interface.
   */
  public void adapt(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations) {
    adapt(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        conHwcPath,
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        CodeUpdaterFactory.spoon());
  }

  /**
   * Adapts reference Java code to a concrete class diagram using an injectable updater factory.
   *
   * <p>The default overloads use a Spoon-backed updater factory. Callers can provide another
   * implementation as long as the factory returns a fresh updater for every call.
   */
  public void adapt(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations,
      CodeUpdaterFactory updaterFactory) {

    Objects.requireNonNull(updaterFactory, "updaterFactory");

    // load CD models
    ASTCDCompilationUnit conCD = JavaLoader.parseCD(concreteCD.getPath());
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(referenceCD.getPath());
    MappingConformanceService conformanceService = new MappingConformanceService(confParams);

    if (useConcretization) {
      new ConcretizationService(confParams).completeConcreteCD(conCD, refCD, mappings);
    }
    CDModelIndex conIndex = CDModelIndex.of(conCD);
    AdaptedCodeMerger codeMerger = new AdaptedCodeMerger();
    OutputCodeService outputCode = new OutputCodeService();

    // Build incarnation contexts for all mappings upfront
    Map<String, IncarnationContext> mappingContexts =
        new AdaptationContextFactory(confParams, conformanceService)
            .buildContexts(refCD, conCD, mappings, useConcretization);

    if (!useConcretization) {
      AdaptationConflictDetector.validate(
          refCD,
          conCD,
          mappings,
          mappingContexts,
          confParams,
          useCommonParentForMultipleIncarnations);
    }

    // Compute aggregated concrete->grouping mappings so updaters can apply replacements in the AST
    Map<String, String> groupingAgg =
        useCommonParentForMultipleIncarnations
            ? new GroupingMappingService().compute(mappingContexts)
            : Collections.emptyMap();

    // Validate all mappings
    Map<String, CDConformanceChecker> checkers = new HashMap<>();
    Map<String, CodeValidator> validators = new HashMap<>();

    if (!useConcretization) {
      for (String mapping : mappings) {
        CodeValidator validator = new CodeValidator(refCD, adapterParams);
        validators.put(mapping, validator);
      }
    } else if (mappings.size() == 1) {
      // Single mapping - check conformance individually
      String mapping = mappings.iterator().next();
      CDConformanceChecker checker = conformanceService.newChecker();
      boolean mappingValid = conformanceService.checkOrFalse(checker, conCD, refCD, mapping);

      if (!mappingValid) {
        // Adaptation should continue using the stereotype-based fallback to construct
        // incarnations and let the updater generate missing members.
        Log.warn("The concrete CD is not conform to the Reference CD for mapping: " + mapping + " - will use stereotype-based fallback");
      }

      checkers.put(mapping, checker);

      // Verify incarnation mapping was created (warn, not error)
      if (checker.getIncarnationMapping() == null) {
        Log.warn("CDConformanceChecker did not create incarnation mapping for mapping '" + mapping + "' - will rely on stereotypes");
      } else {
        Log.info("CDConformanceChecker created incarnation mapping for mapping '" + mapping + "' with " +
            checker.getIncarnationMapping().getClass().getSimpleName(), "adapt");
      }

      CodeValidator validator = new CodeValidator(refCD, adapterParams);
      if (!validator.isValid(refCD, refHwcPath)) {
        Log.error("The reference code is not valid for the reference CD for mapping: " + mapping);
      }
      validators.put(mapping, validator);
    } else {
      // Multiple mappings - run conformance check for each mapping
      // The CDConformanceChecker now handles adapter pattern method ambiguity correctly
      // by matching interface methods only with interface methods and class methods only with class methods
      for (String mapping : mappings) {
        CDConformanceChecker checker = conformanceService.newChecker();
        boolean mappingValid = conformanceService.checkOrFalse(checker, conCD, refCD, mapping);

        if (mappingValid) {
          Log.info("Mapping '" + mapping + "' passed conformance check", "CodeAdapter");
        } else {
          Log.warn("Mapping '" + mapping + "' failed conformance check - will use stereotype-based fallback for this mapping");
        }

        checkers.put(mapping, checker);
        validators.put(mapping, new CodeValidator(refCD, adapterParams));
      }
    }

    // Clean output directory
    JavaLoader.removeDirectory(outputPath);

    // Load reference code for adaptation
    Set<ASTOrdinaryCompilationUnit> refCode = JavaLoader.readJavaCode(refHwcPath);
    Set<ASTOrdinaryCompilationUnit> adaptedCode = new HashSet<>();

    for (String mapping : mappings) {
      if (!mappingContexts.containsKey(mapping)) {
        continue; // Skip mappings that failed to build contexts
      }

      // Determine whether there are reference types with multiple incarnations for this mapping
      IncarnationContext ctx = mappingContexts.get(mapping);
      // collect all reference->incarnations entries
      Map<ISymbol, List<ISymbol>> refToInc = ctx.getReferenceToIncarnations();

      // Find reference type symbols that have multiple incarnations. Field and method
      // multi-incarnations are handled inside a single application so repeatable
      // templates can expand them without running the whole adapter once per member.
      List<ISymbol> multiIncRefTypes = new ArrayList<>();
      for (Map.Entry<ISymbol, List<ISymbol>> e : refToInc.entrySet()) {
        if (e.getValue() != null && e.getValue().size() > 1) {
          if (e.getKey() instanceof de.monticore.cdbasis._symboltable.CDTypeSymbol) {
            multiIncRefTypes.add(e.getKey());
          }
        }
      }

      // If no multi-incarnation reference types, run the original single-shot adaptation once
      if (multiIncRefTypes.isEmpty()) {
        // Create a fresh copy of reference code for this mapping
        Set<ASTOrdinaryCompilationUnit> mappingRefCode = new HashSet<>();
        for (ASTOrdinaryCompilationUnit unit : refCode) {
          mappingRefCode.add(unit.deepClone());
        }
        mappingRefCode =
            codeMerger.filterCodeForMapping(
                mappingRefCode, validators.get(mapping), mappingContexts.get(mapping));
        if (mappingRefCode.isEmpty()) {
          continue;
        }

        // Create temp directory for this mapping
        Path tempPath = outputPath.resolve("_temp_" + mapping);
        try {
          FileUtils.deleteQuietly(tempPath.toFile());
          Files.createDirectories(tempPath);

          // Write cloned AST to temp directory as .java files
          JavaLoader.printAST(mappingRefCode, tempPath);

          CodeUpdater updater = prepareUpdater(updaterFactory, tempPath, groupingAgg);

          // Use BasicUpdateHandler for each mapping
          BasicUpdateHandler singleHandler = new BasicUpdateHandler(
              refCD, conCD, conHwcPath,
              checkers.get(mapping),
              updater,
              validators.get(mapping),
              mappingContexts.get(mapping),
              useCommonParentForMultipleIncarnations);

          // Process this mapping on the reference code copy
          singleHandler.handleUpdate(mappingRefCode);

          // Print output to temp directory
          updater.printCode();

          // Read the processed output back from temp directory
          Set<ASTOrdinaryCompilationUnit> processedCode = JavaLoader.readJavaCode(tempPath);
          // Split compilation units that contain multiple top-level types into one file per type
          processedCode = codeMerger.splitCompilationUnitsByType(processedCode);

          // Merge with accumulated result
          adaptedCode = codeMerger.mergeAdaptedCode(adaptedCode, processedCode);

          // Clean up temp directory
          FileUtils.deleteQuietly(tempPath.toFile());

        } catch (IOException e) {
          Log.error("Failed to process mapping '" + mapping + "': " + e.getMessage());
          // Continue with other mappings
        }
        continue;
      }

      // For each top-level reference type that has multiple incarnations, run adaptation once per concrete incarnation.
      for (ISymbol refTypeSymbol : multiIncRefTypes) {
        List<ISymbol> incarnations = refToInc.get(refTypeSymbol);
        if (incarnations == null) continue;

        for (ISymbol concreteTypeSymbol : incarnations) {
          // Create a fresh copy of reference code for this incarnation
          Set<ASTOrdinaryCompilationUnit> mappingRefCode = new HashSet<>();
          for (ASTOrdinaryCompilationUnit unit : refCode) {
            mappingRefCode.add(unit.deepClone());
          }
          mappingRefCode =
              codeMerger.filterCodeForMapping(
                  mappingRefCode, validators.get(mapping), mappingContexts.get(mapping));
          if (mappingRefCode.isEmpty()) {
            continue;
          }

          // Create temp directory for this incarnation
          Path tempPath = outputPath.resolve("_temp_" + mapping + "_" + concreteTypeSymbol.getName());
          try {
            FileUtils.deleteQuietly(tempPath.toFile());
            Files.createDirectories(tempPath);

            // Write cloned AST to temp directory as .java files
            JavaLoader.printAST(mappingRefCode, tempPath);

            CodeUpdater updater = prepareUpdater(updaterFactory, tempPath, groupingAgg);

            // Build a selector that prefers incarnations belonging to the current concrete type
            final String targetTypeName = concreteTypeSymbol.getName();
            Set<String> attrNames = new HashSet<>();
            conIndex
                .type(targetTypeName)
                .ifPresent(
                    conType ->
                        conIndex.attributes(conType.getName()).forEach(attribute -> attrNames.add(attribute.getName())));

            // simple IncarnationSelector that prefers concrete symbols matching the target type
            IncarnationSelector selector = new IncarnationSelector() {
              @Override
              public ISymbol selectIncarnation(ISymbol referenceSymbol, List<ISymbol> availableIncarnations, IncarnationSelector.SelectionContext context) {
                if (availableIncarnations == null || availableIncarnations.isEmpty()) return null;
                // prefer a type incarnation with the exact target name
                for (ISymbol s : availableIncarnations) {
                  if (s.getName().equals(targetTypeName)) return s;
                }
                // prefer an attribute incarnation whose name exists on the target type
                for (ISymbol s : availableIncarnations) {
                  if (attrNames.contains(s.getName())) return s;
                }
                // fallback to first
                return availableIncarnations.get(0);
              }
            };

            // Use MultiIncarnationUpdateHandler so selection is applied
            // Provide the specific incarnation context for this mapping so symbol resolution
            // can select correct concrete symbols when building generated names.
            IncarnationContext mappingContext = mappingContexts.get(mapping);
            MultiIncarnationUpdateHandler handler = new MultiIncarnationUpdateHandler(
                refCD, conCD, conHwcPath,
                checkers.get(mapping),
                updater,
                validators.get(mapping),
                selector,
                mappingContexts,
                mappingContext,
                mapping,
                useCommonParentForMultipleIncarnations);

            // Process this mapping for the specific concrete incarnation
            handler.handleUpdate(mappingRefCode);

            // Print output to temp directory
            updater.printCode();

            // Read the processed output back from temp directory
            Set<ASTOrdinaryCompilationUnit> processedCode = JavaLoader.readJavaCode(tempPath);
            // Split compilation units that contain multiple top-level types into one file per type
            processedCode = codeMerger.splitCompilationUnitsByType(processedCode);

            // Merge with accumulated result
            adaptedCode = codeMerger.mergeAdaptedCode(adaptedCode, processedCode);

            // Clean up temp directory
            FileUtils.deleteQuietly(tempPath.toFile());

          } catch (IOException e) {
            Log.error("Failed to process mapping '" + mapping + "' for incarnation '" + concreteTypeSymbol.getName() + "': " + e.getMessage());
            // Continue with other incarnations
          }
        }
      }
    }

    // Final output to destination
    if (!adaptedCode.isEmpty()) {
      Set<ASTOrdinaryCompilationUnit> concreteCode =
          Files.exists(conHwcPath) ? JavaLoader.readJavaCode(conHwcPath) : new LinkedHashSet<>();
      Set<ASTOrdinaryCompilationUnit> finalCode =
          codeMerger.mergeAdaptedCodeIntoConcreteBase(concreteCode, adaptedCode, conCD);

      JavaLoader.printAST(finalCode, outputPath);
      // Clean up @Adapt annotations and invalid imports in both adaptation modes.
      outputCode.cleanCode(outputPath, updaterFactory);
    }
    outputCode.copyConcreteFiles(conHwcPath, outputPath);
  }

  private CodeUpdater prepareUpdater(
      CodeUpdaterFactory updaterFactory, Path tempPath, Map<String, String> groupingMappings) {
    CodeUpdater updater =
        Objects.requireNonNull(updaterFactory.createUpdater(), "updaterFactory.createUpdater()");
    updater.setCodePath(tempPath);
    updater.setGroupingMappings(groupingMappings);
    updater.setOutputDirectory(tempPath);
    return updater;
  }

  /**
   * Removes adapter-only metadata from generated Java and lets Spoon perform the formatting pass.
   * Text post-processing is intentionally limited to import lines so comments, literals, generics,
   * and operators are not rewritten by hand.
   *
   * @param codePath generated Java directory
   */
  static void cleanCode(Path codePath) {
    new OutputCodeService().cleanCode(codePath, CodeUpdaterFactory.spoon());
  }
}
