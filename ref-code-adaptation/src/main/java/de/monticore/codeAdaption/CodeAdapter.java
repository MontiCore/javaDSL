package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.context.AdaptationContextFactory;
import de.monticore.codeAdaption.context.AdaptationContextFactory.AdaptationContextResult;
import de.monticore.codeAdaption.context.ConcretizationService;
import de.monticore.codeAdaption.context.GroupingMappingService;
import de.monticore.codeAdaption.context.MappingConformanceService;
import de.monticore.codeAdaption.dependency.ReferenceCodeDependencySelector;
import de.monticore.codeAdaption.dependency.ReferenceCodeSelection;
import de.monticore.codeAdaption.MappingAdaptationRunner.AdaptationPass;
import de.monticore.codeAdaption.handler.multiIncarnation.AdaptationConflictDetector;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.CDImportProjector;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.symboltable.ISymbol;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toCollection;

/**
 * Adapts handwritten reference Java to a concrete class diagram through
 * mapping-specific transformation passes.
 *
 * <p>A failed run never publishes its staging directory over existing output. In concretization
 * mode, only the completed or partially completed concrete CD is retained there as a diagnostic.
 */
public class CodeAdapter {
  /** Maximum number of Cartesian multi-incarnation selections accepted for one source unit. */
  private static final int MAX_TYPE_SELECTIONS = 1024;

  private final Set<AdapterParam> adapterParams;
  private final Set<CDConfParameter> confParams;

  @FunctionalInterface
  private interface FinalCodeComposer {
    FinalCodeComposition compose(
        AdaptedCodeMerger merger,
        Set<ASTOrdinaryCompilationUnit> concreteCode,
        Set<ASTOrdinaryCompilationUnit> adaptedCode,
        CDModelIndex concreteIndex);
  }

  private record FinalCodeComposition(
      Set<ASTOrdinaryCompilationUnit> code, Map<String, String> topToPublicSelfTypes) {}

  /**
   * Creates an adapter with explicit Java matching and CD-conformance policies.
   *
   * @param adapterParams enabled Java matching strategies and unmatched-element policies
   * @param confParams enabled CD conformance parameters
   */
  public CodeAdapter(Set<AdapterParam> adapterParams, Set<CDConfParameter> confParams) {
    this.adapterParams = Set.copyOf(Objects.requireNonNull(adapterParams, "adapterParams"));
    this.confParams = Set.copyOf(Objects.requireNonNull(confParams, "confParams"));
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
   * @throws CodeAdaptationException if validation or deterministic adaptation fails
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
   * Adapts reference Java without a concrete handwritten-code source tree.
   *
   * @param referenceCD reference class diagram
   * @param concreteCD concrete class diagram
   * @param mappings mapping stereotype names to apply
   * @param refHwcPath reference handwritten-code directory
   * @param outputPath output directory
   */
  public void adaptWithoutConcreteCode(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path outputPath) {
    adaptWithoutConcreteCode(
        referenceCD, concreteCD, mappings, refHwcPath, outputPath, false, true);
  }

  /**
   * Adapts reference Java without concrete HWC.
   *
   * <p>This overload is the no-concrete-input form of TOP separation. Because no concrete HWC
   * declarations exist, every adapted type is emitted directly under its adapted concrete name.
   */
  public void adaptWithTopSeparation(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path outputPath) {
    adaptWithTopSeparation(
        referenceCD, concreteCD, mappings, refHwcPath, outputPath, false, true);
  }

  /**
   * Keeps adapted implementations separate from matching concrete HWC through TOP inheritance.
   */
  public void adaptWithTopSeparation(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath) {
    adaptWithTopSeparation(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        conHwcPath,
        outputPath,
        false,
        true);
  }

  /**
   * Adapts reference Java code to a concrete class diagram.
   *
   * <p>With {@code useConcretization=true}, cdconcretization may complete the concrete CD before
   * adaptation. With {@code useConcretization=false}, incarnation contexts are derived manually from
   * stereotypes and deterministic name rules, conflicts are reported before the output directory is
   * cleaned, and the concrete CD is not changed.
   *
   * <p>{@code useCommonParentForMultipleIncarnations} permits manual adaptation to represent an
   * exact group of concrete incarnations through a common concrete interface. For example, if
   * {@code Payment} maps to {@code CreditCard} and {@code Invoice}, and those are exactly the
   * concrete implementers of {@code PaymentMethod}, shared Java declarations may use {@code
   * PaymentMethod}. A broader interface that also has unrelated implementers is not selected.
   *
   * @param referenceCD reference class diagram
   * @param concreteCD concrete class diagram
   * @param mappings mapping stereotype names to apply
   * @param refHwcPath reference handwritten-code directory
   * @param conHwcPath concrete handwritten-code directory
   * @param outputPath output directory published only after a successful run
   * @param useConcretization whether to complete the concrete CD before adaptation
   * @param useCommonParentForMultipleIncarnations whether a common concrete interface whose
   *     implementers exactly match an incarnation group may replace the individual incarnation
   *     types in shared Java declarations
   * @throws CodeAdaptationException if validation, conflict detection, or adaptation fails
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
        true);
  }

  /**
   * Full adaptation API with explicit control over persistence of the concretized class diagram.
   *
   * @param persistConcretizedCD whether to write the working concrete CD to the output directory;
   *     ignored when {@code useConcretization} is false
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
      boolean persistConcretizedCD) {
    adaptInternal(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        Optional.of(Objects.requireNonNull(conHwcPath, "conHwcPath")),
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        persistConcretizedCD,
        CodeAdapter::mergeFinalCode);
  }

  /**
   * Adapts reference Java without a concrete handwritten-code input.
   */
  public void adaptWithoutConcreteCode(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations) {
    adaptWithoutConcreteCode(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        true);
  }

  /** No-concrete-code API with explicit control over concretized-CD persistence. */
  public void adaptWithoutConcreteCode(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations,
      boolean persistConcretizedCD) {
    adaptInternal(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        Optional.empty(),
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        persistConcretizedCD,
        CodeAdapter::directFinalCode);
  }

  /** Full TOP-separation API without a concrete handwritten-code source tree. */
  public void adaptWithTopSeparation(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations) {
    adaptWithTopSeparation(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        true);
  }

  /**
   * Full TOP-separation API without concrete HWC and with explicit concretized-CD persistence.
   */
  public void adaptWithTopSeparation(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations,
      boolean persistConcretizedCD) {
    adaptInternal(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        Optional.empty(),
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        persistConcretizedCD,
        CodeAdapter::composeWithTopSeparation);
  }

  /** Full TOP-separation API with an optional-per-type concrete HWC source tree. */
  public void adaptWithTopSeparation(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations) {
    adaptWithTopSeparation(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        conHwcPath,
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        true);
  }

  /** Full TOP-separation API with explicit control over concretized-CD persistence. */
  public void adaptWithTopSeparation(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations,
      boolean persistConcretizedCD) {
    adaptInternal(
        referenceCD,
        concreteCD,
        mappings,
        refHwcPath,
        Optional.of(Objects.requireNonNull(conHwcPath, "conHwcPath")),
        outputPath,
        useConcretization,
        useCommonParentForMultipleIncarnations,
        persistConcretizedCD,
        CodeAdapter::composeWithTopSeparation);
  }

  private void adaptInternal(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Optional<Path> conHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations,
      boolean persistConcretizedCD,
      FinalCodeComposer finalCodeComposer) {
    AdaptationWorkspace workspace =
        new AdaptationWorkspace(refHwcPath, conHwcPath, outputPath);
    workspace.validateReadOnlyInput(referenceCD.toPath(), "reference class diagram");
    workspace.validateReadOnlyInput(concreteCD.toPath(), "concrete class diagram");
    Path normalizedRefHwcPath = workspace.referenceSource();
    Optional<Path> normalizedConHwcPath = workspace.concreteSourceOptional();
    SortedSet<String> validatedMappingsInOrder = validatedMappings(mappings);

    // load CD models
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(referenceCD.getPath());
    ASTCDCompilationUnit conCD = JavaLoader.parseCD(concreteCD.getPath());
    CDModelIndex inputConcreteIndex = CDModelIndex.of(conCD);
    CDModelIndex referenceIndex = CDModelIndex.of(refCD);
    MappingConformanceService conformanceService = new MappingConformanceService(confParams);
    Path concretizedCDFileName = concreteCD.toPath().getFileName();

    if (useConcretization) {
      ASTCDCompilationUnit completedCD = conCD.deepClone();
      try {
        new ConcretizationService(confParams)
            .completeConcreteCDInPlace(completedCD, refCD, validatedMappingsInOrder);
      } catch (RuntimeException | Error failure) {
        if (persistConcretizedCD) {
          persistConcretizedCDAfterFailure(
              completedCD, workspace.output().resolve(concretizedCDFileName), failure);
        }
        throw failure;
      }
      conCD = completedCD;
      if (persistConcretizedCD) {
        persistConcretizedCD(conCD, workspace.output().resolve(concretizedCDFileName));
      }
    }
    CDModelIndex conIndex = CDModelIndex.of(conCD);
    AdaptedCodeMerger codeMerger = new AdaptedCodeMerger();
    OutputCodeService outputCode = new OutputCodeService();

    // Build each context and checker once, then reuse that exact checker state in handlers.
    Map<String, AdaptationContextResult> contextResults =
        new AdaptationContextFactory(confParams, conformanceService)
            .buildResults(referenceIndex, conIndex, validatedMappingsInOrder, useConcretization);
    Map<String, IncarnationContext> incarnationContextsByMapping = new LinkedHashMap<>();
    Map<String, CDConformanceChecker> checkers = new LinkedHashMap<>();
    Map<String, CodeValidator> validators = new LinkedHashMap<>();
    Set<ASTOrdinaryCompilationUnit> refCode = JavaLoader.readJavaCode(normalizedRefHwcPath);
    ReferenceCodeDependencySelector dependencySelector =
        useConcretization
            ? new ReferenceCodeDependencySelector(
                normalizedRefHwcPath, refCode, referenceIndex.typeNames())
            : null;
    Map<String, ReferenceCodeSelection> codeSelections = new LinkedHashMap<>();
    for (String mapping : validatedMappingsInOrder) {
      AdaptationContextResult result = contextResults.get(mapping);
      incarnationContextsByMapping.put(mapping, result.context());
      checkers.put(mapping, result.checker());
      CodeValidator validator =
          useConcretization
              ? new CodeValidator(
                  refCD,
                  adapterParams,
                  CodeValidator.ValidationPolicy.PRESERVE_CONCRETIZATION_HELPERS)
              : new CodeValidator(refCD, adapterParams);
      validators.put(mapping, validator);
      if (useConcretization) {
        Set<String> mappedRoots =
            mappedRootTypeIdentities(
                refCode, validator, result.context(), referenceIndex);
        codeSelections.put(mapping, dependencySelector.select(mappedRoots));
      } else {
        codeSelections.put(mapping, emptyCodeSelection());
      }
    }

    SortedSet<String> fallbackMappings =
        contextResults.entrySet().stream()
            .filter(entry -> !entry.getValue().conformanceValid())
            .map(Map.Entry::getKey)
            .collect(
                toCollection(TreeSet::new));
    if (!useConcretization || !fallbackMappings.isEmpty()) {
      Set<String> mappingsToValidate =
          useConcretization ? fallbackMappings : validatedMappingsInOrder;
      Map<String, IncarnationContext> contextsToValidate = new LinkedHashMap<>();
      mappingsToValidate.forEach(
          mapping -> contextsToValidate.put(mapping, incarnationContextsByMapping.get(mapping)));
      AdaptationConflictDetector.validate(
          referenceIndex,
          conIndex,
          mappingsToValidate,
          contextsToValidate,
          confParams,
          useCommonParentForMultipleIncarnations);
    }

    // Validate before creating staging directories or changing existing output.
    for (String mapping : validatedMappingsInOrder) {
      CodeValidator validator = validators.get(mapping);
      boolean valid =
          validator != null
              && (useConcretization
                  ? validator.isValid(
                      refCD,
                      refCode,
                      codeSelections.get(mapping).selectedTypeIdentities())
                  : validator.isValid(refCD, refCode));
      if (!valid) {
        throw new CodeAdaptationException(
            "Reference code is not valid for the reference CD (mapping '" + mapping + "')");
      }
    }

    CDImportProjector.project(refCode, refCD, conCD);
    Path stagingPath = workspace.createStagingDirectory();
    boolean published = false;
    try {
      Set<ASTOrdinaryCompilationUnit> adaptedCode = new LinkedHashSet<>();
      MappingAdaptationRunner passRunner =
          new MappingAdaptationRunner(
              workspace,
              codeMerger,
              stagingPath,
              referenceIndex,
              conIndex,
              inputConcreteIndex,
              useCommonParentForMultipleIncarnations);

      for (String mapping : validatedMappingsInOrder) {
        if (!incarnationContextsByMapping.containsKey(mapping)) {
          continue; // Skip mappings that failed to build contexts
        }

        IncarnationContext ctx = incarnationContextsByMapping.get(mapping);
        Map<String, String> groupingMappings =
            useCommonParentForMultipleIncarnations
                ? new GroupingMappingService().compute(ctx)
                : Collections.emptyMap();
        Set<ASTOrdinaryCompilationUnit> mappingCode = cloneUnits(refCode);
        mappingCode = codeMerger.splitCompilationUnitsByType(mappingCode);
        ReferenceCodeSelection codeSelection = codeSelections.get(mapping);
        mappingCode =
            codeMerger.filterCodeForMapping(
                mappingCode,
                validators.get(mapping),
                ctx,
                referenceIndex,
                codeSelection.selectedTypeIdentities());

        Set<ASTOrdinaryCompilationUnit> mappingAdaptedCode =
            passRunner.run(
                mapping,
                mappingCode,
                buildAdaptationPasses(
                    mappingCode,
                    validators.get(mapping),
                    ctx,
                    referenceIndex,
                    conIndex,
                    inputConcreteIndex,
                    codeSelection,
                    useCommonParentForMultipleIncarnations),
                checkers.get(mapping),
                validators.get(mapping),
                ctx,
                groupingMappings,
                codeSelection.helperTypeIdentities());
        adaptedCode = codeMerger.mergeAdaptedCode(adaptedCode, mappingAdaptedCode);
      }

      // Composition is selected by the public API, while mapping and adaptation stay identical.
      Set<ASTOrdinaryCompilationUnit> concreteCode =
          normalizedConHwcPath.filter(Files::exists)
              .map(JavaLoader::readJavaCode)
              .orElseGet(LinkedHashSet::new);
      concreteCode = codeMerger.splitCompilationUnitsByType(concreteCode);
      FinalCodeComposition finalComposition =
          finalCodeComposer.compose(codeMerger, concreteCode, adaptedCode, conIndex);
      Set<ASTOrdinaryCompilationUnit> finalCode = finalComposition.code();
      if (!finalCode.isEmpty()) {
        JavaLoader.printAST(finalCode, stagingPath);
      }
      normalizedConHwcPath.ifPresent(path -> outputCode.copyConcreteFiles(path, stagingPath));
      if (containsJavaFiles(stagingPath)) {
        // Cleanup runs once after the selected composition has produced the complete Java tree.
        outputCode.cleanCode(stagingPath, finalComposition.topToPublicSelfTypes());
      }
      if (useConcretization && persistConcretizedCD) {
        persistConcretizedCD(conCD, stagingPath.resolve(concretizedCDFileName));
      }
      workspace.publish(stagingPath);
      published = true;
    } finally {
      if (!published) {
        workspace.discard(stagingPath);
      }
    }
  }

  private static void persistConcretizedCD(ASTCDCompilationUnit concreteCD, Path target) {
    JavaLoader.writeFile(target, CD4CodeMill.prettyPrint(concreteCD, true));
  }

  private static void persistConcretizedCDAfterFailure(
      ASTCDCompilationUnit concreteCD, Path target, Throwable originalFailure) {
    try {
      persistConcretizedCD(concreteCD, target);
    } catch (RuntimeException | Error persistenceFailure) {
      originalFailure.addSuppressed(persistenceFailure);
    }
  }

  private static FinalCodeComposition mergeFinalCode(
      AdaptedCodeMerger merger,
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    return new FinalCodeComposition(
        merger.mergeAdaptedCodeIntoConcreteBase(concreteCode, adaptedCode, concreteIndex),
        Map.of());
  }

  private static FinalCodeComposition directFinalCode(
      AdaptedCodeMerger merger,
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    LinkedHashSet<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>(concreteCode);
    result.addAll(adaptedCode);
    return new FinalCodeComposition(result, Map.of());
  }

  private static FinalCodeComposition composeWithTopSeparation(
      AdaptedCodeMerger merger,
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    TopCodeComposer.CompositionResult result =
        new TopCodeComposer(merger)
            .composeWithSelfTypeBindings(concreteCode, adaptedCode, concreteIndex);
    return new FinalCodeComposition(result.code(), result.topToPublicSelfTypes());
  }

  static void validatePaths(Path refHwcPath, Path conHwcPath, Path outputPath) {
    AdaptationWorkspace.validatePaths(refHwcPath, conHwcPath, outputPath);
  }

  private static boolean containsJavaFiles(Path sourcePath) {
    if (!Files.exists(sourcePath)) {
      return false;
    }
    if (!Files.isDirectory(sourcePath)) {
      throw new IllegalArgumentException("Java source path is not a directory: " + sourcePath);
    }
    try (var paths = Files.walk(sourcePath)) {
      return paths.anyMatch(
          path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"));
    } catch (java.io.IOException exception) {
      throw new CodeAdaptationException(
          "Could not inspect Java source directory " + sourcePath, exception);
    }
  }

  private static Set<String> mappedRootTypeIdentities(
      Set<ASTOrdinaryCompilationUnit> units,
      CodeValidator validator,
      IncarnationContext context,
      CDModelIndex referenceIndex) {
    validator.initializeTypeMatcher(units);
    Set<String> roots = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : units) {
      for (var type : unit.getTypeDeclarationList()) {
        Optional<CodeMatching> matching = validator.getMatchedType(type);
        if (matching.isEmpty()) {
          continue;
        }
        if (!matching.get().mustBePerform()) {
          if (matching.get().isExplicitAnnotation()) {
            roots.add(qualifiedTypeIdentity(unit, type.getName()));
          }
          continue;
        }
        boolean mapped =
            matching.get().getReferences().stream()
                .map(reference -> StableElementKey.fromSymbol(reference, referenceIndex))
                .flatMap(Optional::stream)
                .map(context::getIncarnations)
                .anyMatch(incarnations -> !incarnations.isEmpty());
        if (mapped) {
          roots.add(qualifiedTypeIdentity(unit, type.getName()));
        }
      }
    }
    return Set.copyOf(roots);
  }

  private static ReferenceCodeSelection emptyCodeSelection() {
    return new ReferenceCodeSelection(Set.of(), Set.of(), Map.of(), Map.of(), List.of());
  }

  private static Set<ASTOrdinaryCompilationUnit> cloneUnits(
      Set<ASTOrdinaryCompilationUnit> units) {
    Set<ASTOrdinaryCompilationUnit> clones = new LinkedHashSet<>();
    units.forEach(unit -> clones.add(unit.deepClone()));
    return clones;
  }

  private static List<AdaptationPass> buildAdaptationPasses(
      Set<ASTOrdinaryCompilationUnit> units,
      CodeValidator validator,
      IncarnationContext context,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      CDModelIndex inputConcreteIndex,
      ReferenceCodeSelection codeSelection,
      boolean useCommonParentForMultipleIncarnations) {
    List<AdaptationPass> passes = new ArrayList<>();
    boolean hasDefaultPass = false;
    Set<String> defaultOutputTypeNames = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : units) {
      Set<StableElementKey> relevantTypes = new LinkedHashSet<>();
      JavaAstElemCollector collector = collectJavaElements(unit);
      for (String identity : topLevelTypeIdentities(unit)) {
        for (String referenceType :
            codeSelection
                .referencedCDTypeKeysBySourceType()
                .getOrDefault(identity, Set.of())) {
          addRelevantTypeKey(
              StableElementKey.type(referenceType),
              context,
              relevantTypes,
              useCommonParentForMultipleIncarnations);
        }
      }
      for (var type : collector.getAllTypeDeclarations()) {
        addRelevantTypeReferences(
            validator.getMatchedType(type),
            context,
            relevantTypes,
            referenceIndex,
            useCommonParentForMultipleIncarnations);
        for (var field : collector.getAllFieldDeclarations(type)) {
          addRelevantTypeReferences(
              validator.getMatchedField(type, field),
              context,
              relevantTypes,
              referenceIndex,
              useCommonParentForMultipleIncarnations);
        }
        for (var supertype : collector.getAllFSuperTypeDeclarations(type)) {
          addRelevantTypeReferences(
              validator.getMatchedSupertype(type, supertype),
              context,
              relevantTypes,
              referenceIndex,
              useCommonParentForMultipleIncarnations);
        }
        for (var method : collector.getAllMethodDeclarations(type)) {
          addRelevantTypeReferences(
              validator.getMatchedMethod(type, method),
              context,
              relevantTypes,
              referenceIndex,
              useCommonParentForMultipleIncarnations);
          for (var parameter : collector.getAllParameters(type, method)) {
            addRelevantTypeReferences(
                validator.getMatchedParameter(type, method, parameter),
                context,
                relevantTypes,
                referenceIndex,
                useCommonParentForMultipleIncarnations);
          }
          for (var local : collector.getAllLocVariables(type, method)) {
            addRelevantTypeReferences(
                validator.getMatchedLocalVariable(type, method, local),
                context,
                relevantTypes,
                referenceIndex,
                useCommonParentForMultipleIncarnations);
          }
        }
      }
      List<StableElementKey> orderedReferences =
          relevantTypes.stream()
              .sorted(Comparator.comparing(StableElementKey::signature))
              .toList();
      if (orderedReferences.isEmpty()) {
        hasDefaultPass = true;
        defaultOutputTypeNames.addAll(
            expectedOutputTypeIdentities(unit, validator, context, Map.of(), referenceIndex));
      } else {
        for (Map<StableElementKey, IncarnationContext.MappedElement> selection :
            buildCompleteTypeSelections(
                orderedReferences,
                context,
                referenceIndex,
                concreteIndex,
                inputConcreteIndex)) {
          Set<String> outputIdentities =
              new LinkedHashSet<>(
                  expectedOutputTypeIdentities(
                      unit, validator, context, selection, referenceIndex));
          outputIdentities.addAll(codeSelection.helperTypeIdentities());
          passes.add(new AdaptationPass(selection, outputIdentities));
        }
      }
    }
    defaultOutputTypeNames.addAll(codeSelection.helperTypeIdentities());
    if (hasDefaultPass) {
      passes.add(0, new AdaptationPass(Map.of(), Set.copyOf(defaultOutputTypeNames)));
    }
    return passes;
  }

  /**
   * Builds the owner-aware Java element index used while planning multi-incarnation passes for one
   * compilation unit.
   */
  private static JavaAstElemCollector collectJavaElements(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }

  /**
   * Adds reference types that require pass expansion because a performing match has multiple
   * concrete incarnations. Exact common-grouping sets stay in a single grouped pass, and non-type
   * references do not participate in type selection.
   */
  private static void addRelevantTypeReferences(
      Optional<CodeMatching> matching,
      IncarnationContext context,
      Set<StableElementKey> relevantTypes,
      CDModelIndex referenceIndex,
      boolean useCommonParentForMultipleIncarnations) {
    if (matching.isEmpty() || !matching.get().mustBePerform()) {
      return;
    }
    for (ISymbol reference : matching.get().getReferences()) {
      if (!(reference instanceof de.monticore.cdbasis._symboltable.CDTypeSymbol)) {
        continue;
      }
      List<IncarnationContext.MappedElement> incarnations =
          StableElementKey.fromSymbol(reference, referenceIndex)
              .map(context::getIncarnations)
              .orElseGet(List::of);
      if (incarnations.size() < 2
          || (useCommonParentForMultipleIncarnations
              && isExactGroupingSet(context, incarnations))) {
        continue;
      }
      StableElementKey.fromSymbol(reference, referenceIndex)
          .ifPresent(relevantTypes::add);
    }
  }

  private static void addRelevantTypeKey(
      StableElementKey referenceType,
      IncarnationContext context,
      Set<StableElementKey> relevantTypes,
      boolean useCommonParentForMultipleIncarnations) {
    List<IncarnationContext.MappedElement> incarnations =
        context.getIncarnations(referenceType);
    if (incarnations.size() < 2
        || (useCommonParentForMultipleIncarnations
            && isExactGroupingSet(context, incarnations))) {
      return;
    }
    relevantTypes.add(referenceType);
  }

  private static boolean isExactGroupingSet(
      IncarnationContext context, List<IncarnationContext.MappedElement> incarnations) {
    Set<String> incarnationNames =
        incarnations.stream()
            .map(element -> element.key().getName())
            .collect(java.util.stream.Collectors.toSet());
    Set<String> derivedGroupings =
        incarnationNames.stream()
            .map(name -> groupingName(context, name))
            .flatMap(Optional::stream)
            .collect(java.util.stream.Collectors.toSet());
    if (derivedGroupings.size() == 1) {
      String grouping = derivedGroupings.iterator().next();
      boolean everyIncarnationBelongsToGrouping =
          incarnationNames.stream()
              .allMatch(
                  incarnation ->
                      grouping.equals(incarnation)
                          || groupingName(context, incarnation)
                              .filter(grouping::equals)
                              .isPresent());
      if (everyIncarnationBelongsToGrouping) {
        return true;
      }
    }
    for (String possibleGrouping : incarnationNames) {
      boolean foundImplementer = false;
      boolean exact = true;
      for (String incarnation : incarnationNames) {
        if (possibleGrouping.equals(incarnation)) {
          continue;
        }
        Optional<String> grouping = groupingName(context, incarnation);
        foundImplementer |= grouping.isPresent();
        if (grouping.isEmpty() || !possibleGrouping.equals(grouping.get())) {
          exact = false;
          break;
        }
      }
      if (exact && foundImplementer) {
        return true;
      }
    }
    return false;
  }

  private static Set<String> expectedOutputTypeIdentities(
      ASTOrdinaryCompilationUnit unit,
      CodeValidator validator,
      IncarnationContext context,
      Map<StableElementKey, IncarnationContext.MappedElement> selection,
      CDModelIndex referenceIndex) {
    Set<String> result = new LinkedHashSet<>();
    for (var type : unit.getTypeDeclarationList()) {
      Optional<CodeMatching> matching = validator.getMatchedType(type);
      if (matching.isEmpty() || !matching.get().mustBePerform()) {
        result.add(qualifiedTypeIdentity(unit, type.getName()));
        continue;
      }
      boolean resolved = false;
      List<ISymbol> concreteReferences = new ArrayList<>();
      for (ISymbol reference : matching.get().getReferences()) {
        IncarnationContext.MappedElement selected =
            selectedIncarnation(reference, selection, context, referenceIndex);
        concreteReferences.add(selected == null ? reference : selected.symbol());
        resolved |= selected != null;
      }
      String template =
          matching.get().getGenerateTemplate() != null
                  && !matching.get().getGenerateTemplate().isEmpty()
              ? matching.get().getGenerateTemplate()
              : matching.get().getTemplate();
      if (resolved && template != null) {
        result.add(
            qualifiedTypeIdentity(
                unit, MatcherHelper.fillTemplate(template, concreteReferences)));
      } else {
        result.add(qualifiedTypeIdentity(unit, type.getName()));
      }
    }
    return Set.copyOf(result);
  }

  private static Set<String> topLevelTypeIdentities(ASTOrdinaryCompilationUnit unit) {
    return unit.getTypeDeclarationList().stream()
        .map(type -> qualifiedTypeIdentity(unit, type.getName()))
        .collect(toCollection(LinkedHashSet::new));
  }

  private static String qualifiedTypeIdentity(
      ASTOrdinaryCompilationUnit unit, String simpleName) {
    String packageName =
        unit.isPresentPackageDeclaration()
            ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
            : "";
    return packageName.isBlank() ? simpleName : packageName + "." + simpleName;
  }

  private static IncarnationContext.MappedElement selectedIncarnation(
      ISymbol reference,
      Map<StableElementKey, IncarnationContext.MappedElement> selection,
      IncarnationContext context,
      CDModelIndex referenceIndex) {
    Optional<StableElementKey> referenceKey =
        StableElementKey.fromSymbol(reference, referenceIndex);
    IncarnationContext.MappedElement selected = referenceKey.map(selection::get).orElse(null);
    if (selected != null) {
      return selected;
    }
    List<IncarnationContext.MappedElement> incarnations =
        referenceKey.map(context::getIncarnations).orElseGet(List::of);
    return incarnations.size() == 1 ? incarnations.get(0) : null;
  }

  private static SortedSet<String> validatedMappings(Set<String> mappings) {
    if (mappings == null || mappings.isEmpty()) {
      throw new IllegalArgumentException("At least one mapping is required");
    }
    SortedSet<String> ordered = new TreeSet<>();
    for (String mapping : mappings) {
      if (mapping == null || mapping.isBlank()) {
        throw new IllegalArgumentException("Mapping names must not be null or blank");
      }
      ordered.add(mapping);
    }
    return Collections.unmodifiableSortedSet(ordered);
  }

  private static List<Map<StableElementKey, IncarnationContext.MappedElement>>
      buildCompleteTypeSelections(
      List<StableElementKey> referenceTypes,
      IncarnationContext context,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      CDModelIndex inputConcreteIndex) {
    List<Map<StableElementKey, IncarnationContext.MappedElement>> selections = new ArrayList<>();
    selections.add(new LinkedHashMap<>());
    for (StableElementKey referenceType : referenceTypes) {
      List<IncarnationContext.MappedElement> incarnations =
          context.getIncarnations(referenceType).stream()
              .collect(toCollection(ArrayList::new));
      incarnations.removeIf(incarnation -> !concreteIndex.hasType(incarnation.key().getName()));
      String referenceName = referenceType.getName();
      if (incarnations.size() > 1 && !inputConcreteIndex.hasType(referenceName)) {
        incarnations.removeIf(incarnation -> referenceName.equals(incarnation.key().getName()));
      }
      List<IncarnationContext.MappedElement> sameKindIncarnations =
          incarnations.stream()
              .filter(
                  incarnation ->
                      sameTypeKind(
                          referenceIndex.type(referenceName).orElse(null),
                          concreteIndex.type(incarnation.key().getName()).orElse(null)))
              .toList();
      if (!sameKindIncarnations.isEmpty()) {
        incarnations = new ArrayList<>(sameKindIncarnations);
      }
      incarnations.sort(Comparator.comparing(incarnation -> incarnation.key().signature()));
      if (incarnations.isEmpty()) {
        throw new CodeAdaptationException(
            "No incarnations available for reference type '" + referenceType.getName() + "'");
      }
      long newSize = (long) selections.size() * incarnations.size();
      if (newSize > MAX_TYPE_SELECTIONS) {
        throw new CodeAdaptationException(
            "Multi-incarnation type selections exceed the safe limit of "
                + MAX_TYPE_SELECTIONS
                + " runs");
      }
      List<Map<StableElementKey, IncarnationContext.MappedElement>> expanded =
          new ArrayList<>((int) newSize);
      for (Map<StableElementKey, IncarnationContext.MappedElement> selection : selections) {
        for (IncarnationContext.MappedElement incarnation : incarnations) {
          Map<StableElementKey, IncarnationContext.MappedElement> copy =
              new LinkedHashMap<>(selection);
          copy.put(referenceType, incarnation);
          expanded.add(Map.copyOf(copy));
        }
      }
      selections = expanded;
    }
    return selections;
  }

  private static boolean sameTypeKind(
      de.monticore.cdbasis._ast.ASTCDType reference,
      de.monticore.cdbasis._ast.ASTCDType incarnation) {
    return (reference instanceof de.monticore.cdbasis._ast.ASTCDClass
            && incarnation instanceof de.monticore.cdbasis._ast.ASTCDClass)
        || (reference instanceof de.monticore.cdinterfaceandenum._ast.ASTCDInterface
            && incarnation
                instanceof de.monticore.cdinterfaceandenum._ast.ASTCDInterface)
        || (reference instanceof de.monticore.cdinterfaceandenum._ast.ASTCDEnum
            && incarnation instanceof de.monticore.cdinterfaceandenum._ast.ASTCDEnum);
  }

  private static Optional<String> groupingName(
      IncarnationContext context, String concreteTypeName) {
    return context
        .getGroupingFor(StableElementKey.type(concreteTypeName))
        .map(grouping -> grouping.key().getName());
  }

}
