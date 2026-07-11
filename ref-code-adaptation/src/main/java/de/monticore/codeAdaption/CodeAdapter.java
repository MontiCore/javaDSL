package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.context.AdaptationContextFactory;
import de.monticore.codeAdaption.context.AdaptationContextFactory.AdaptationContextResult;
import de.monticore.codeAdaption.context.ConcretizationService;
import de.monticore.codeAdaption.context.GroupingMappingService;
import de.monticore.codeAdaption.context.MappingConformanceService;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.handler.multiIncarnation.*;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdaterMill;
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

public class CodeAdapter {
  private static final int MAX_TYPE_SELECTIONS = 1024;

  private final Set<AdapterParam> adapterParams;
  private final Set<CDConfParameter> confParams;

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
    // MontiCore mills and global symbol scopes are process-global. Serialize complete runs rather
    // than presenting unsafe pseudo-concurrency around only the updater instance.
    synchronized (CodeAdapter.class) {
      adaptInternal(
          referenceCD,
          concreteCD,
          mappings,
          refHwcPath,
          conHwcPath,
          outputPath,
          useConcretization,
          useCommonParentForMultipleIncarnations);
    }
  }

  private void adaptInternal(
      File referenceCD,
      File concreteCD,
      Set<String> mappings,
      Path refHwcPath,
      Path conHwcPath,
      Path outputPath,
      boolean useConcretization,
      boolean useCommonParentForMultipleIncarnations) {
    AdaptationWorkspace workspace =
        new AdaptationWorkspace(refHwcPath, conHwcPath, outputPath);
    workspace.validateReadOnlyInput(referenceCD.toPath(), "reference class diagram");
    workspace.validateReadOnlyInput(concreteCD.toPath(), "concrete class diagram");
    Path normalizedRefHwcPath = workspace.referenceSource();
    Path normalizedConHwcPath = workspace.concreteSource();
    SortedSet<String> orderedMappings = validatedMappings(mappings);

    // load CD models
    ASTCDCompilationUnit conCD = JavaLoader.parseCD(concreteCD.getPath());
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(referenceCD.getPath());
    CDModelIndex inputConcreteIndex = CDModelIndex.of(conCD);
    CDModelIndex referenceIndex = CDModelIndex.of(refCD);
    MappingConformanceService conformanceService = new MappingConformanceService(confParams);

    if (useConcretization) {
      conCD =
          new ConcretizationService(confParams)
              .completeConcreteCD(conCD, refCD, orderedMappings);
    }
    CDModelIndex conIndex = CDModelIndex.of(conCD);
    AdaptedCodeMerger codeMerger = new AdaptedCodeMerger();
    OutputCodeService outputCode = new OutputCodeService();

    // Build each context and checker once, then reuse that exact checker state in handlers.
    Map<String, AdaptationContextResult> contextResults =
        new AdaptationContextFactory(confParams, conformanceService)
            .buildResults(refCD, conCD, orderedMappings, useConcretization);
    Map<String, IncarnationContext> mappingContexts = new LinkedHashMap<>();
    Map<String, CDConformanceChecker> checkers = new LinkedHashMap<>();
    Map<String, CodeValidator> validators = new LinkedHashMap<>();
    for (String mapping : orderedMappings) {
      AdaptationContextResult result = contextResults.get(mapping);
      mappingContexts.put(mapping, result.context());
      checkers.put(mapping, result.checker());
      validators.put(mapping, new CodeValidator(refCD, adapterParams));
    }

    if (!useConcretization) {
      AdaptationConflictDetector.validate(
          refCD,
          conCD,
          orderedMappings,
          mappingContexts,
          confParams,
          useCommonParentForMultipleIncarnations);
    }

    // Validate before creating staging directories or changing existing output.
    for (String mapping : orderedMappings) {
      CodeValidator validator = validators.get(mapping);
      if (validator == null || !validator.isValid(refCD, normalizedRefHwcPath)) {
        throw new CodeAdaptationException(
            "Reference code is not valid for the reference CD (mapping '" + mapping + "')");
      }
    }

    Set<ASTOrdinaryCompilationUnit> refCode = JavaLoader.readJavaCode(normalizedRefHwcPath);
    CDImportProjector.project(refCode, refCD, conCD);
    Path stagingPath = workspace.createStagingDirectory();
    boolean published = false;
    try {
    Set<ASTOrdinaryCompilationUnit> adaptedCode = new LinkedHashSet<>();

    for (String mapping : orderedMappings) {
      if (!mappingContexts.containsKey(mapping)) {
        continue; // Skip mappings that failed to build contexts
      }

      IncarnationContext ctx = mappingContexts.get(mapping);
      Map<String, String> groupingMappings =
          useCommonParentForMultipleIncarnations
              ? new GroupingMappingService().compute(ctx)
              : Collections.emptyMap();
      Set<ASTOrdinaryCompilationUnit> mappingCode = cloneUnits(refCode);
      mappingCode = codeMerger.splitCompilationUnitsByType(mappingCode);
      mappingCode =
          codeMerger.filterCodeForMapping(
              mappingCode, validators.get(mapping), ctx, referenceIndex);

      for (AdaptationPass pass :
          buildAdaptationPasses(
              mappingCode,
              validators.get(mapping),
              ctx,
              referenceIndex,
              conIndex,
              inputConcreteIndex,
              useCommonParentForMultipleIncarnations)) {
        // Load the complete mapping source set so Spoon can update cross-file references. The
        // pass still controls which transformed top-level types are retained below.
        Set<ASTOrdinaryCompilationUnit> mappingRefCode = cloneUnits(mappingCode);
        Path tempPath = workspace.createMappingDirectory(stagingPath);
        try {
          JavaLoader.printAST(mappingRefCode, tempPath);
          CodeUpdater updater = prepareUpdater(tempPath, groupingMappings);
          BasicUpdateHandler handler;
          if (pass.typeSelection().isEmpty()) {
            handler =
                new BasicUpdateHandler(
                    refCD,
                    conCD,
                    normalizedConHwcPath,
                    checkers.get(mapping),
                    updater,
                    validators.get(mapping),
                    ctx,
                    useCommonParentForMultipleIncarnations);
          } else {
            handler =
                new MultiIncarnationUpdateHandler(
                    refCD,
                    conCD,
                    normalizedConHwcPath,
                    checkers.get(mapping),
                    updater,
                    validators.get(mapping),
                    createSelector(pass.typeSelection(), ctx, referenceIndex, conIndex),
                    Map.of(mapping, ctx),
                    ctx,
                    mapping,
                    useCommonParentForMultipleIncarnations);
          }

          handler.handleUpdate(mappingRefCode);
          updater.printCode();
          Set<ASTOrdinaryCompilationUnit> processedCode =
              codeMerger.splitCompilationUnitsByType(JavaLoader.readJavaCode(tempPath));
          processedCode =
              processedCode.stream()
                  .filter(
                      unit ->
                          unit.getTypeDeclarationList().stream()
                              .anyMatch(type -> pass.outputTypeNames().contains(type.getName())))
                  .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
          adaptedCode = codeMerger.mergeAdaptedCode(adaptedCode, processedCode);
        } catch (RuntimeException | AssertionError e) {
          String selection =
              pass.typeSelection().isEmpty()
                  ? ""
                  : " for type selection " + describeSelection(pass.typeSelection());
          throw new CodeAdaptationException(
              "Failed to process mapping '" + mapping + "'" + selection, e);
        } finally {
          CodeUpdaterMill.reset();
          workspace.discard(tempPath);
        }
      }
    }

    // Final output to destination
    if (!adaptedCode.isEmpty()) {
      Set<ASTOrdinaryCompilationUnit> concreteCode =
          Files.exists(normalizedConHwcPath)
              ? JavaLoader.readJavaCode(normalizedConHwcPath)
              : new LinkedHashSet<>();
      Set<ASTOrdinaryCompilationUnit> finalCode =
          codeMerger.mergeAdaptedCodeIntoConcreteBase(concreteCode, adaptedCode, conCD);

      JavaLoader.printAST(finalCode, stagingPath);
      // Clean up @Adapt annotations and invalid imports in both adaptation modes.
      outputCode.cleanCode(stagingPath);
    }
    outputCode.copyConcreteFiles(normalizedConHwcPath, stagingPath);
    workspace.publish(stagingPath);
    published = true;
    } finally {
      CodeUpdaterMill.reset();
      if (!published) {
        workspace.discard(stagingPath);
      }
    }
  }

  static void validatePaths(Path refHwcPath, Path conHwcPath, Path outputPath) {
    AdaptationWorkspace.validatePaths(refHwcPath, conHwcPath, outputPath);
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
      boolean useCommonParentForMultipleIncarnations) {
    List<AdaptationPass> passes = new ArrayList<>();
    Set<ASTOrdinaryCompilationUnit> defaultUnits = new LinkedHashSet<>();
    Set<String> defaultOutputTypeNames = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : units) {
      Map<StableElementKey, ISymbol> relevantTypes = new LinkedHashMap<>();
      JavaAstElemCollector collector = collectJavaElements(unit);
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
      List<ISymbol> orderedReferences =
          relevantTypes.entrySet().stream()
              .sorted(Map.Entry.comparingByKey(Comparator.comparing(StableElementKey::signature)))
              .map(Map.Entry::getValue)
              .toList();
      if (orderedReferences.isEmpty()) {
        Map<ISymbol, ISymbol> selection = Map.of();
        defaultUnits.add(unit);
        defaultOutputTypeNames.addAll(
            expectedOutputTypeNames(unit, validator, context, selection, referenceIndex));
      } else {
        for (Map<ISymbol, ISymbol> selection :
            buildCompleteTypeSelections(
                orderedReferences,
                context,
                referenceIndex,
                concreteIndex,
                inputConcreteIndex)) {
          passes.add(
              new AdaptationPass(
                  Set.of(unit),
                  selection,
                  expectedOutputTypeNames(unit, validator, context, selection, referenceIndex)));
        }
      }
    }
    if (!defaultUnits.isEmpty()) {
      passes.add(
          0,
          new AdaptationPass(
              Set.copyOf(defaultUnits), Map.of(), Set.copyOf(defaultOutputTypeNames)));
    }
    return passes;
  }

  private static JavaAstElemCollector collectJavaElements(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }

  private static void addRelevantTypeReferences(
      Optional<de.monticore.codeAdaption.matcher.CodeMatching> matching,
      IncarnationContext context,
      Map<StableElementKey, ISymbol> relevantTypes,
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
          .ifPresent(key -> relevantTypes.put(key, reference));
    }
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

  private static Set<String> expectedOutputTypeNames(
      ASTOrdinaryCompilationUnit unit,
      CodeValidator validator,
      IncarnationContext context,
      Map<ISymbol, ISymbol> selection,
      CDModelIndex referenceIndex) {
    Set<String> result = new LinkedHashSet<>();
    for (var type : unit.getTypeDeclarationList()) {
      Optional<CodeMatching> matching = validator.getMatchedType(type);
      if (matching.isEmpty() || !matching.get().mustBePerform()) {
        result.add(type.getName());
        continue;
      }
      boolean resolved = false;
      List<ISymbol> concreteReferences = new ArrayList<>();
      for (ISymbol reference : matching.get().getReferences()) {
        ISymbol selected =
            selectedIncarnation(reference, selection, context, referenceIndex);
        concreteReferences.add(selected == null ? reference : selected);
        resolved |= selected != null;
      }
      String template =
          matching.get().getGenerateTemplate() != null
                  && !matching.get().getGenerateTemplate().isEmpty()
              ? matching.get().getGenerateTemplate()
              : matching.get().getTemplate();
      if (resolved && template != null) {
        result.add(MatcherHelper.fillTemplate(template, concreteReferences));
      } else {
        result.add(type.getName());
      }
    }
    return Set.copyOf(result);
  }

  private static ISymbol selectedIncarnation(
      ISymbol reference,
      Map<ISymbol, ISymbol> selection,
      IncarnationContext context,
      CDModelIndex referenceIndex) {
    ISymbol selected = selection.get(reference);
    Optional<StableElementKey> referenceKey =
        StableElementKey.fromSymbol(reference, referenceIndex);
    if (selected == null) {
      selected =
          selection.entrySet().stream()
              .filter(
                  entry ->
                      referenceKey.equals(
                          StableElementKey.fromSymbol(entry.getKey(), referenceIndex)))
              .map(Map.Entry::getValue)
              .findFirst()
              .orElse(null);
    }
    if (selected != null) {
      return selected;
    }
    List<IncarnationContext.MappedElement> incarnations =
        referenceKey.map(context::getIncarnations).orElseGet(List::of);
    return incarnations.size() == 1 ? incarnations.get(0).symbol() : null;
  }

  private static IncarnationSelector createSelector(
      Map<ISymbol, ISymbol> typeSelection,
      IncarnationContext context,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex) {
    return (referenceSymbol, availableIncarnations, ignored) -> {
      if (availableIncarnations == null || availableIncarnations.isEmpty()) {
        return null;
      }
      ISymbol selectedType = typeSelection.get(referenceSymbol);
      if (selectedType != null) {
        return selectedType;
      }
      Optional<String> referenceOwner =
          StableElementKey.fromSymbol(referenceSymbol, referenceIndex)
              .flatMap(StableElementKey::getOwnerType);
      if (referenceOwner.isPresent()) {
        Optional<ISymbol> selectedOwner =
            typeSelection.entrySet().stream()
                .filter(
                    entry ->
                        StableElementKey
                            .fromSymbol(entry.getKey(), referenceIndex)
                            .filter(key -> key.getKind() == StableElementKey.Kind.TYPE)
                            .map(StableElementKey::getName)
                            .filter(referenceOwner.get()::equals)
                            .isPresent())
                .map(Map.Entry::getValue)
                .findFirst();
        if (selectedOwner.isPresent()) {
          List<ISymbol> ownerMatches =
              availableIncarnations.stream()
                  .filter(
                      candidate ->
                          StableElementKey
                              .fromSymbol(candidate, concreteIndex)
                              .flatMap(StableElementKey::getOwnerType)
                              .filter(selectedOwner.get().getName()::equals)
                              .isPresent())
                  .toList();
          if (ownerMatches.size() == 1) {
            return ownerMatches.get(0);
          }
        }
      }

      Map<ISymbol, Integer> scores = new IdentityHashMap<>();
      for (ISymbol candidate : availableIncarnations) {
        String identity =
            StableElementKey
                .fromSymbol(candidate, concreteIndex)
                .map(StableElementKey::toString)
                .orElse(candidate.getName())
                .toLowerCase(Locale.ROOT);
        int score =
            typeSelection.values().stream()
                .map(ISymbol::getName)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .distinct()
                .filter(identity::contains)
                .mapToInt(String::length)
                .sum();
        scores.put(candidate, score);
      }
      int bestScore = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
      if (bestScore > 0) {
        List<ISymbol> best =
            scores.entrySet().stream()
                .filter(entry -> entry.getValue() == bestScore)
                .map(Map.Entry::getKey)
                .toList();
        if (best.size() == 1) {
          return best.get(0);
        }
      }
      return availableIncarnations.stream()
          .sorted(Comparator.comparing(ISymbol::getName))
          .findFirst()
          .orElse(null);
    };
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

  private static List<Map<ISymbol, ISymbol>> buildCompleteTypeSelections(
      List<ISymbol> referenceTypes,
      IncarnationContext context,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      CDModelIndex inputConcreteIndex) {
    List<Map<ISymbol, ISymbol>> selections = new ArrayList<>();
    selections.add(new IdentityHashMap<>());
    for (ISymbol referenceType : referenceTypes) {
      List<ISymbol> incarnations =
          context
              .getIncarnations(
                  StableElementKey.fromSymbol(referenceType, referenceIndex).orElseThrow())
              .stream()
              .map(IncarnationContext.MappedElement::symbol)
              .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
      incarnations.removeIf(incarnation -> !concreteIndex.hasType(incarnation.getName()));
      String referenceName =
          StableElementKey
              .fromSymbol(referenceType, referenceIndex)
              .map(StableElementKey::getName)
              .orElse(referenceType.getName());
      if (incarnations.size() > 1 && !inputConcreteIndex.hasType(referenceName)) {
        incarnations.removeIf(incarnation -> referenceName.equals(incarnation.getName()));
      }
      List<ISymbol> sameKindIncarnations =
          incarnations.stream()
              .filter(
                  incarnation ->
                      sameTypeKind(
                          referenceIndex.type(referenceName).orElse(null),
                          concreteIndex.type(incarnation.getName()).orElse(null)))
              .toList();
      if (!sameKindIncarnations.isEmpty()) {
        incarnations = new ArrayList<>(sameKindIncarnations);
      }
      incarnations.sort(Comparator.comparing(ISymbol::getName));
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
      List<Map<ISymbol, ISymbol>> expanded = new ArrayList<>((int) newSize);
      for (Map<ISymbol, ISymbol> selection : selections) {
        for (ISymbol incarnation : incarnations) {
          Map<ISymbol, ISymbol> copy = new IdentityHashMap<>(selection);
          copy.put(referenceType, incarnation);
          expanded.add(copy);
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

  private static String describeSelection(Map<ISymbol, ISymbol> selection) {
    return selection.entrySet().stream()
        .sorted(Map.Entry.comparingByKey(Comparator.comparing(ISymbol::getName)))
        .map(entry -> entry.getKey().getName() + "=" + entry.getValue().getName())
        .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
  }

  private record AdaptationPass(
      Set<ASTOrdinaryCompilationUnit> code,
      Map<ISymbol, ISymbol> typeSelection,
      Set<String> outputTypeNames) {}

  private CodeUpdater prepareUpdater(Path tempPath, Map<String, String> groupingMappings) {
    CodeUpdaterMill.reset();
    CodeUpdater updater = CodeUpdaterMill.getUpdater();
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
    new OutputCodeService().cleanCode(codePath);
  }
}
