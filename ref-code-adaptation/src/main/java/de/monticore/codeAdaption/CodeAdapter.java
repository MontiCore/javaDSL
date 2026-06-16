package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.cddiff.CDDiffUtil;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.symboltable.ISymbol;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.handler.multiIncarnation.*;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.spoonUpdater.SpoonUpdater;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.Constants;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.JavaSourcePostProcessor;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import org.apache.commons.io.FileUtils;
import spoon.Launcher;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class CodeAdapter {
  protected Set<AdapterParam> adapterParams;
  protected Set<CDConfParameter> confParams;

  public CodeAdapter(Set<AdapterParam> adapterParams, Set<CDConfParameter> confParams) {
    this.adapterParams = adapterParams;
    this.confParams = confParams;
  }

  // Textual post-print grouping replacement helper was removed in for
  // AST-level replacements applied by `SpoonUpdater`. All grouping mappings are
  // now computed via `computeGroupingMappings(...)` and passed to updaters through
  // the `CodeUpdater.setGroupingMappings(...)` API before printing.

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

    // load CD models
    ASTCDCompilationUnit conCD = JavaLoader.parseCD(concreteCD.getPath());
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(referenceCD.getPath());

    if (useConcretization) {
      concretizeConcreteCD(conCD, refCD, mappings);
    }

    // Build incarnation contexts for all mappings upfront
    Map<String, IncarnationContext> mappingContexts =
        buildIncarnationContexts(refCD, conCD, mappings, useConcretization);

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
            ? computeGroupingMappings(mappingContexts)
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
      CDConformanceChecker checker = new CDConformanceChecker(confParams);
      boolean mappingValid = false;
      try {
        mappingValid = checker.checkConformance(conCD, refCD, mapping);
      } catch (Throwable t) {
        Log.warn("Conformance checker threw during check for mapping '" + mapping + "': " + t.getMessage() + " - will use stereotype-based fallback");
        mappingValid = false;
      }

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
        CDConformanceChecker checker = new CDConformanceChecker(confParams);
        boolean mappingValid = checker.checkConformance(conCD, refCD, mapping);

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
        mappingRefCode = filterCodeForMapping(mappingRefCode, validators.get(mapping), mappingContexts.get(mapping));
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

           // Create NEW SpoonUpdater for this mapping's code
           CodeUpdater updater = new SpoonUpdater();
           updater.setCodePath(tempPath);
           // Provide grouping mappings so SpoonUpdater can apply AST-level replacements
           updater.setGroupingMappings(groupingAgg);
           updater.setOutputDirectory(tempPath);

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
          Set<File> outputFiles = updater.printCode();

          // Read the processed output back from temp directory
          Set<ASTOrdinaryCompilationUnit> processedCode = JavaLoader.readJavaCode(tempPath);
          // Split compilation units that contain multiple top-level types into one file per type
          processedCode = splitCompilationUnitsByType(processedCode);

          // Merge with accumulated result
          adaptedCode = mergeAdaptedCode(adaptedCode, processedCode);

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
          mappingRefCode = filterCodeForMapping(mappingRefCode, validators.get(mapping), mappingContexts.get(mapping));
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

            // Create new SpoonUpdater for this incarnation's code
            CodeUpdater updater = new SpoonUpdater();
            updater.setCodePath(tempPath);
            // Provide grouping mappings so SpoonUpdater can apply AST-level replacements
            updater.setGroupingMappings(groupingAgg);
            updater.setOutputDirectory(tempPath);

            // Build a selector that prefers incarnations belonging to the current concrete type
            final String targetTypeName = concreteTypeSymbol.getName();
            // collect attribute names for the concrete type from conCD
            // TODO: I have written preprocessing collecters for all elements and hierarchies in cd4analysis.
            //  Add them also in this project as this is the 4-th time where I resolve the types and iterate
            Set<String> attrNames = new HashSet<>();
            for (ASTCDType conType : CDDiffUtil.getAllCDTypes(conCD)) {
              if (conType.getName().equals(targetTypeName)) {
                for (var a : conType.getCDAttributeList()) {
                  attrNames.add(a.getName());
                }
                break;
              }
            }

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
            Set<File> outputFiles = updater.printCode();

            // Read the processed output back from temp directory
            Set<ASTOrdinaryCompilationUnit> processedCode = JavaLoader.readJavaCode(tempPath);
            // Split compilation units that contain multiple top-level types into one file per type
            processedCode = splitCompilationUnitsByType(processedCode);

            // Merge with accumulated result
            adaptedCode = mergeAdaptedCode(adaptedCode, processedCode);

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
          mergeAdaptedCodeIntoConcreteBase(concreteCode, adaptedCode, conCD);

      JavaLoader.printAST(finalCode, outputPath);
      if (!useConcretization) {
        // Clean up @Adapt annotations and other metadata from adapted code.
        cleanCode(outputPath);
      }
    }
    copyConcreteFiles(conHwcPath, outputPath);
  }

  /**
   * Builds incarnation contexts for all mappings.
   * This collects all available incarnations for each reference symbol across all mappings.
   */
  // TODO: I don't think this will be used as the other method as preference
  private Map<String, IncarnationContext> buildIncarnationContexts(
      ASTCDCompilationUnit refCD, ASTCDCompilationUnit conCD, Set<String> mappings) {
    return buildIncarnationContexts(refCD, conCD, mappings, false);
  }

  private Map<String, IncarnationContext> buildIncarnationContexts(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Set<String> mappings,
      boolean useConcretizationMappings) {

    Map<String, IncarnationContext> contexts = new HashMap<>();

    if (!useConcretizationMappings) {
      ManualIncarnationContextBuilder builder =
          new ManualIncarnationContextBuilder(refCD, conCD, confParams);
      for (String mapping : mappings) {
        contexts.put(mapping, builder.buildContextForMapping(mapping));
      }
      return contexts;
    }

    for (String mapping : mappings) {
      // For multi-mapping, skip conformance checks since they may be partial
      // For single mapping, use standard checker
      CDConformanceChecker checker = new CDConformanceChecker(confParams);

      // Run conformance check for this mapping (works for both single and multi-mapping)
      // The CDConformanceChecker now handles adapter pattern method ambiguity correctly
      boolean mappingValid = false;
      try {
        mappingValid = checker.checkConformance(conCD, refCD, mapping);
         } catch (Throwable e) {
          Log.warn("Mapping '" + mapping + "' threw exception during conformance check: " + e.getMessage() + " - using stereotype-based fallback");
          mappingValid = false;
        }

        if (!mappingValid) {
          // Attempt to build an IncarnationContext. The builder will use stereotypes as a fallback
          // if the conformance checker did not produce an incarnation mapping.
          Log.warn("Mapping '" + mapping + "' failed conformance check - will use stereotype-based fallback for this mapping");
        } else {
          Log.info("Mapping '" + mapping + "' passed conformance check with incarnation mapping: " +
              (checker.getIncarnationMapping() != null ? checker.getIncarnationMapping().getClass().getSimpleName() : "null"), "CodeAdapter");
        }

      // Use IncarnationContextBuilder to properly extract incarnations
      IncarnationContextBuilder builder = new IncarnationContextBuilder(checker, refCD, conCD);
      IncarnationContext context = builder.buildContextForMapping(mapping, !useConcretizationMappings);
      contexts.put(mapping, context);
    }

    return contexts;
  }

  private void concretizeConcreteCD(
      ASTCDCompilationUnit conCD, ASTCDCompilationUnit refCD, Set<String> mappings) {
    ConcretizationCompleter completer = new ConcretizationCompleter(confParams);
    try {
      completer.completeCD(conCD, refCD, new ArrayList<>(mappings));
      Log.info("Concretized concrete CD before code adaptation", "CodeAdapter");
    } catch (Throwable t) {
      Log.warn(
          "CD concretization failed before code adaptation: "
              + t.getMessage()
              + " - continuing with available conformance mappings");
    }
  }

  /**
   * Compute aggregated mapping from concrete simple-name -> grouping simple-name
   * by consulting each IncarnationContext. This does not modify files; it only
   * prepares a map that can be applied to Spoon models.
   */
  private Map<String, String> computeGroupingMappings(Map<String, IncarnationContext> mappingContexts) {
    Map<String, String> agg = new HashMap<>();
    for (IncarnationContext ctx : mappingContexts.values()) {
      for (Map.Entry<ISymbol, List<ISymbol>> e : ctx.getReferenceToIncarnations().entrySet()) {
        List<ISymbol> incs = e.getValue();
        if (incs == null) continue;
        for (ISymbol s : incs) {
          if (s == null) continue;
          var g = ctx.findGroupingTypeForImplementer(s.getName());
          if (g.isPresent() && !g.get().equals(s.getName())) {
            agg.put(s.getName(), g.get());
          }
        }
      }
    }
    return agg;
  }

  private Set<ASTOrdinaryCompilationUnit> mergeAdaptedCode(
      Set<ASTOrdinaryCompilationUnit> actualCode, Set<ASTOrdinaryCompilationUnit> newAdaptedCode) {
    if (actualCode.isEmpty()) {
      // First mapping - deduplicate files with same simple name
      return deduplicateBySimpleName(newAdaptedCode);
    }

    // Deduplicate new files first (same simple name, prefer package subdir)
    Set<ASTOrdinaryCompilationUnit> deduplicatedNew = deduplicateBySimpleName(newAdaptedCode);

    for (ASTOrdinaryCompilationUnit newAdapted : deduplicatedNew) {
      String newSimpleName = AdapterUtils.getSimpleFileName(newAdapted);

      Optional<ASTOrdinaryCompilationUnit> actual =
          actualCode.stream()
              .filter(f -> AdapterUtils.getSimpleFileName(f).equals(newSimpleName))
              .findAny();

      if (actual.isEmpty()) {
        // New file - add it
        actualCode.add(newAdapted);
      } else {
        // Existing file - merge ASTs (first mapping wins for conflicts)
        actualCode.remove(actual.get());
        actualCode.add(AdapterUtils.mergeAsts(actual.get(), newAdapted));
      }
    }
    return actualCode;
  }

  private Set<ASTOrdinaryCompilationUnit> filterCodeForMapping(
      Set<ASTOrdinaryCompilationUnit> javaFiles,
      CodeValidator validator,
      IncarnationContext context) {
    validator.initializeTypeMatcher(javaFiles);
    Set<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : javaFiles) {
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      unit.accept(traverser);

      boolean mappedTopLevel =
          collector.getAllTypeDeclarations().stream()
              .map(validator::getMatchedType)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .flatMap(matching -> matching.getReferences().stream())
              .anyMatch(ref -> {
                List<ISymbol> incarnations = context.getIncarnations(ref);
                return incarnations != null && !incarnations.isEmpty();
              });
      boolean ignoredTopLevel =
          collector.getAllTypeDeclarations().stream()
              .map(validator::getMatchedType)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .anyMatch(matching -> !matching.mustBePerform());
      if (mappedTopLevel || ignoredTopLevel) {
        result.add(unit);
      }
    }
    return result;
  }

  /**
   * Builds the final generated code on top of the concrete handwritten classes. Adapted pattern
   * types with the same simple file name are merged into the concrete class, while reference-only
   * template artifacts such as Builder, OSubject, or OObserver are filtered out.
   */
  private Set<ASTOrdinaryCompilationUnit> mergeAdaptedCodeIntoConcreteBase(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      ASTCDCompilationUnit conCD) {

    Set<String> concreteTypeNames =
        CDDiffUtil.getAllCDTypes(conCD).stream()
            .map(ASTCDType::getName)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

    Set<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>(deduplicateBySimpleName(concreteCode));

    for (ASTOrdinaryCompilationUnit adaptedUnit : deduplicateBySimpleName(adaptedCode)) {
      String adaptedTypeName = getPrimaryTypeName(adaptedUnit).orElse("");
      if (!shouldKeepAdaptedUnit(adaptedTypeName, concreteTypeNames)) {
        continue;
      }

      String adaptedFileName = AdapterUtils.getSimpleFileName(adaptedUnit);
      Optional<ASTOrdinaryCompilationUnit> concreteMatch =
          result.stream()
              .filter(unit -> AdapterUtils.getSimpleFileName(unit).equals(adaptedFileName))
              .findFirst();

      if (concreteMatch.isPresent()) {
        result.remove(concreteMatch.get());
        result.add(AdapterUtils.mergeAsts(concreteMatch.get(), adaptedUnit));
      } else {
        result.add(adaptedUnit);
      }
    }

    return deduplicateBySimpleName(result);
  }

  private boolean shouldKeepAdaptedUnit(String typeName, Set<String> concreteTypeNames) {
    if (typeName == null || typeName.isEmpty()) {
      return false;
    }
    if (concreteTypeNames.contains(typeName)) {
      return true;
    }
    // Keep generated companion types that are named from a concrete type like
    // StudentRepository, PersonBuilder.
    return concreteTypeNames.stream()
        .anyMatch(concreteName -> typeName.startsWith(concreteName) || typeName.endsWith(concreteName));
  }

  private Optional<String> getPrimaryTypeName(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector.getAllTypeDeclarations().stream().map(ASTTypeDeclaration::getName).findFirst();
  }

  private static void copyConcreteFiles(Path conHwcPath, Path outputPath) {
    if (!Files.exists(conHwcPath)) {
      try {
        Files.createDirectories(outputPath);
      } catch (IOException e) {
        throw new IllegalStateException("Failed to create output directory " + outputPath, e);
      }
      return;
    }
    try (var paths = Files.walk(conHwcPath)) {
      paths
          .filter(Files::isRegularFile)
          .forEach(
              p -> {
                try {
                  Path rel = conHwcPath.relativize(p);
                  Path target = outputPath.resolve(rel);
                  Files.createDirectories(target.getParent());
                  if (!Files.exists(target)) {
                    Files.copy(p, target);
                  }
                } catch (IOException e) {
                  throw new IllegalStateException(
                      "Failed to copy concrete file '" + p + "' to output", e);
                }
              });
    } catch (IOException e) {
      throw new IllegalStateException("Failed to include concrete handwritten code", e);
    }
  }

  /**
   * Split compilation units that contain multiple top-level type declarations into separate
   * compilation units, one per type. This also sets the SourcePositionStart file name so that
   * JavaLoader.printAST will write each type into its own file named <TypeName>.java.
   */
  private Set<ASTOrdinaryCompilationUnit> splitCompilationUnitsByType(Set<ASTOrdinaryCompilationUnit> units) {
    Set<ASTOrdinaryCompilationUnit> res = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : units) {
      // collect type declarations using the Java AST collector
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      unit.accept(traverser);

      List<ASTTypeDeclaration> types = new ArrayList<>(collector.getAllTypeDeclarations());
      if (types.size() <= 1) {
        res.add(unit);
        continue;
      }

      // create one compilation unit per contained type
      for (ASTTypeDeclaration t : types) {
        ASTOrdinaryCompilationUnit copy = unit.deepClone();
        // remove all types except the one we want
        // remove by iterating current type declarations on the copy and removing those that
        // don't match the desired type name
        JavaAstElemCollector copyCollector = new JavaAstElemCollector();
        JavaDSLTraverser copyTraverser = JavaDSLMill.traverser();
        copyTraverser.add4JavaDSL(copyCollector);
        copy.accept(copyTraverser);
        for (ASTTypeDeclaration ct : copyCollector.getAllTypeDeclarations()) {
          if (!ct.getName().equals(t.getName())) {
            copy.removeTypeDeclaration(ct);
          }
        }

        // set a sensible source filename so JavaLoader.printAST writes <TypeName>.java
        String fileName = t.getName() + ".java";
        copy.get_SourcePositionStart().setFileName(fileName);
        Log.info("splitCompilationUnitsByType -> created unit: " + fileName, "CodeAdapter");
        res.add(copy);
      }
    }
    return res;
  }

  /**
   * Deduplicate files by simple filename, preferring files in package subdirectories.
   * Removes duplicates like "MyClass.java" at root when "de/foo/MyClass.java" exists.
   */
  private Set<ASTOrdinaryCompilationUnit> deduplicateBySimpleName(
      Set<ASTOrdinaryCompilationUnit> files) {
    Map<String, ASTOrdinaryCompilationUnit> bySimpleName = new LinkedHashMap<>();

    for (ASTOrdinaryCompilationUnit file : files) {
      String simpleName = AdapterUtils.getSimpleFileName(file);
      String fullPath = AdapterUtils.getFileName(file);

      ASTOrdinaryCompilationUnit existing = bySimpleName.get(simpleName);
      if (existing == null) {
        // First occurrence
        bySimpleName.put(simpleName, file);
      } else {
        // Prefer file in deeper directory
        String existingPath = AdapterUtils.getFileName(existing);
        if (JavaSourceNames.pathDepth(fullPath) > JavaSourceNames.pathDepth(existingPath)) {
          bySimpleName.put(simpleName, file);
        }
      }
    }

    return new LinkedHashSet<>(bySimpleName.values());
  }

  /**
   * Removes adapter-only metadata from generated Java and lets Spoon perform the formatting pass.
   * Text post-processing is intentionally limited to import lines so comments, literals, generics,
   * and operators are not rewritten by hand.
   *
   * @param codePath generated Java directory
   */
  static void cleanCode(Path codePath) {
    Path formattedPath = codePath.resolveSibling(codePath.getFileName() + "_formatted");
    try {
      FileUtils.deleteQuietly(formattedPath.toFile());
      Files.createDirectories(formattedPath);
      Map<String, Path> originalFilesByName = javaFilesBySimpleName(codePath);

      Launcher launcher = new Launcher();
      launcher.getEnvironment().setAutoImports(true);
      launcher.getEnvironment().setNoClasspath(true);
      try (var paths = Files.walk(codePath)) {
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(p -> launcher.addInputResource(p.toAbsolutePath().toString()));
      }
      launcher.buildModel();

      List<CtAnnotation<?>> annotations =
          new ArrayList<>(launcher.getModel().getElements(new TypeFilter<>(CtAnnotation.class)));
      annotations.stream().filter(CodeAdapter::isAdaptAnnotation).forEach(CtAnnotation::delete);

      launcher.setSourceOutputDirectory(formattedPath.toFile());
      launcher.prettyprint();

      try (var paths = Files.walk(formattedPath)) {
        paths
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(
                p -> {
                  try {
                    Path target =
                        originalFilesByName.getOrDefault(
                            p.getFileName().toString(), codePath.resolve(formattedPath.relativize(p)));
                    Files.createDirectories(target.getParent());
                    copyReplacingWithRetry(p, target);
                  } catch (IOException e) {
                    throw new IllegalStateException(
                        "Failed to copy formatted file '" + p + "' to output", e);
                  }
                });
      }

      JavaSourcePostProcessor.processDirectory(codePath);
      Log.info("CodeAdapter.cleanCode: Cleanup completed", "CodeAdapter");
    } catch (IOException e) {
      Log.error("CodeAdapter.cleanCode: Failed to clean code: " + e.getMessage());
    } finally {
      FileUtils.deleteQuietly(formattedPath.toFile());
    }
  }

  private static Map<String, Path> javaFilesBySimpleName(Path codePath) throws IOException {
    Map<String, Path> result = new LinkedHashMap<>();
    try (var paths = Files.walk(codePath)) {
      paths
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> result.putIfAbsent(path.getFileName().toString(), path));
    }
    return result;
  }

  /**
   * Replaces a generated file with a bounded retry for short-lived Windows file locks left by the
   * parser/pretty-printer pipeline.
   */
  private static void copyReplacingWithRetry(Path source, Path target) throws IOException {
    IOException lastException = null;
    for (int attempt = 0; attempt < 5; attempt++) {
      try {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return;
      } catch (IOException e) {
        lastException = e;
        try {
          Thread.sleep(50L * (attempt + 1));
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
    throw lastException;
  }

  static String postProcessGeneratedJavaSource(String content) {
    return JavaSourcePostProcessor.process(content);
  }

  private static boolean isAdaptAnnotation(CtAnnotation<?> annotation) {
    CtTypeReference<?> annotationType = annotation.getAnnotationType();
    if (annotationType != null) {
      String simpleName = annotationType.getSimpleName();
      String qualifiedName = annotationType.getQualifiedName();
      if (Constants.ANNOT_NAME.equals(simpleName)
          || simpleName.endsWith("." + Constants.ANNOT_NAME)
          || Constants.ANNOT_PACKAGE.equals(qualifiedName)
          || qualifiedName.endsWith("." + Constants.ANNOT_NAME)) {
        return true;
      }
    }
    String rendered = annotation.toString().trim();
    return rendered.startsWith("@" + Constants.ANNOT_NAME)
        || rendered.startsWith("@." + Constants.ANNOT_NAME)
        || rendered.contains("." + Constants.ANNOT_NAME + "(")
        || rendered.contains("." + Constants.ANNOT_NAME + "[");
  }
}
