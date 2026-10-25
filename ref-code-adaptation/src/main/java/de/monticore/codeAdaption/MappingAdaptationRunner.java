package de.monticore.codeAdaption;

import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdaterMill;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Executes the complete isolated updater lifecycle for one mapping's adaptation passes. */
final class MappingAdaptationRunner {
  /**
   * One deterministic multi-incarnation choice and the top-level Java types retained from it.
   *
   * <p>{@code typeSelection} maps reference type keys to the one concrete incarnation used by this
   * pass, for example {@code Payment -> CreditCard}. {@code outputTypeIdentities} contains
   * package-qualified concrete Java top-level names such as {@code billing.CreditCardAdapter}; all
   * other transformed units are discarded after serving as cross-file resolution context.
   */
  record AdaptationPass(
      Map<StableElementKey, IncarnationContext.MappedElement> typeSelection,
      Set<String> outputTypeIdentities) {
    AdaptationPass {
      typeSelection = Map.copyOf(typeSelection);
      outputTypeIdentities = Set.copyOf(outputTypeIdentities);
    }

    String describeSelection() {
      return typeSelection.entrySet().stream()
          .sorted(Map.Entry.comparingByKey(Comparator.comparing(StableElementKey::signature)))
          .map(entry -> entry.getKey().signature() + "=" + entry.getValue().key().signature())
          .collect(Collectors.joining(", ", "[", "]"));
    }
  }

  private final AdaptationWorkspace workspace;
  private final AdaptedCodeMerger codeMerger;
  private final Path stagingPath;
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;
  private final CDModelIndex inputConcreteIndex;
  private final boolean useCommonParentForMultipleIncarnations;
  private final HelperVariantRegistry helperVariantRegistry = new HelperVariantRegistry();

  MappingAdaptationRunner(
      AdaptationWorkspace workspace,
      AdaptedCodeMerger codeMerger,
      Path stagingPath,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      CDModelIndex inputConcreteIndex,
      boolean useCommonParentForMultipleIncarnations) {
    this.workspace = workspace;
    this.codeMerger = codeMerger;
    this.stagingPath = stagingPath;
    this.referenceIndex = referenceIndex;
    this.concreteIndex = concreteIndex;
    this.inputConcreteIndex = inputConcreteIndex;
    this.useCommonParentForMultipleIncarnations =
        useCommonParentForMultipleIncarnations;
  }

  /**
   * Runs every planned pass for one mapping and merges the retained output units.
   *
   * @param mapping stereotype name identifying the isolated incarnation context
   * @param mappingCode complete filtered source set loaded into every pass for cross-file resolution
   * @param passes concrete incarnation selections to execute
   * @param checker usable conformance checker, or {@code null} for manual mapping
   * @param validator matcher/validation state for the same mapping
   * @param context reference-element to concrete-incarnation mappings for this mapping only
   * @param groupingMappings incarnation simple name to common grouping-type simple name
   * @param helperTypeIdentities package-qualified top-level helper names retained by every pass
   */
  Set<ASTOrdinaryCompilationUnit> run(
      String mapping,
      Set<ASTOrdinaryCompilationUnit> mappingCode,
      List<AdaptationPass> passes,
      CDConformanceChecker checker,
      CodeValidator validator,
      IncarnationContext context,
      Map<String, String> groupingMappings,
      Set<String> helperTypeIdentities) {
    Set<ASTOrdinaryCompilationUnit> adaptedCode = new LinkedHashSet<>();
    for (AdaptationPass pass : passes) {
      // Load the complete mapping source set so Spoon can update cross-file references. The pass
      // still controls which transformed top-level types are retained below.
      Set<ASTOrdinaryCompilationUnit> mappingRefCode = cloneUnits(mappingCode);
      Path tempPath = workspace.createMappingDirectory(stagingPath);
      try {
        JavaLoader.printAST(mappingRefCode, tempPath);
        CodeUpdater updater = prepareUpdater(tempPath, groupingMappings);
        BasicUpdateHandler handler =
            new BasicUpdateHandler(
                referenceIndex,
                concreteIndex,
                inputConcreteIndex,
                checker,
                updater,
                validator,
                context,
                pass.typeSelection(),
                useCommonParentForMultipleIncarnations);

        handler.handleUpdate(mappingRefCode);
        updater.printCode();
        Set<ASTOrdinaryCompilationUnit> processedCode =
            codeMerger.splitCompilationUnitsByType(JavaLoader.readJavaCode(tempPath));
        processedCode =
            processedCode.stream()
                .filter(
                    unit ->
                        declaredTypeIdentities(unit).stream()
                            .anyMatch(pass.outputTypeIdentities()::contains))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        processedCode =
            removeDuplicateHelperVariants(processedCode, helperTypeIdentities, mapping, pass);
        adaptedCode = codeMerger.mergeAdaptedCode(adaptedCode, processedCode);
      } catch (CodeAdaptationException exception) {
        String selection =
            pass.typeSelection().isEmpty()
                ? ""
                : " for type selection " + pass.describeSelection();
        throw new CodeAdaptationException(
            "Failed to process mapping '"
                + mapping
                + "'"
                + selection
                + ": "
                + exception.getMessage(),
            exception);
      } catch (RuntimeException | AssertionError exception) {
        String selection =
            pass.typeSelection().isEmpty()
                ? ""
                : " for type selection " + pass.describeSelection();
        throw new CodeAdaptationException(
            "Failed to process mapping '" + mapping + "'" + selection, exception);
      } finally {
        CodeUpdaterMill.reset();
        workspace.discard(tempPath);
      }
    }
    return adaptedCode;
  }

  private CodeUpdater prepareUpdater(Path tempPath, Map<String, String> groupingMappings) {
    CodeUpdaterMill.reset();
    CodeUpdater updater = CodeUpdaterMill.getUpdater();
    updater.setCodePath(tempPath);
    updater.setGroupingMappings(groupingMappings);
    updater.setOutputDirectory(tempPath);
    return updater;
  }

  private static Set<ASTOrdinaryCompilationUnit> cloneUnits(
      Set<ASTOrdinaryCompilationUnit> units) {
    Set<ASTOrdinaryCompilationUnit> clones = new LinkedHashSet<>();
    units.forEach(unit -> clones.add(unit.deepClone()));
    return clones;
  }

  private Set<ASTOrdinaryCompilationUnit> removeDuplicateHelperVariants(
      Set<ASTOrdinaryCompilationUnit> incoming,
      Set<String> helperTypeIdentities,
      String mapping,
      AdaptationPass pass) {
    Set<ASTOrdinaryCompilationUnit> retained = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : incoming) {
      String helperIdentity =
          declaredTypeIdentities(unit).stream()
              .filter(helperTypeIdentities::contains)
              .findFirst()
              .orElse(null);
      if (helperIdentity == null
          || helperVariantRegistry.register(
              helperIdentity, unit, mapping, pass.describeSelection())) {
        retained.add(unit);
      }
    }
    return retained;
  }

  private static Set<String> declaredTypeIdentities(ASTOrdinaryCompilationUnit unit) {
    String packageName =
        unit.isPresentPackageDeclaration()
            ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
            : "";
    return unit.getTypeDeclarationList().stream()
        .map(type -> packageName.isBlank() ? type.getName() : packageName + "." + type.getName())
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
