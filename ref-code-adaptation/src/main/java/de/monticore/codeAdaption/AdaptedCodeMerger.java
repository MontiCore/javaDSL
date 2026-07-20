package de.monticore.codeAdaption;

import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Merges, filters, splits, and deduplicates Java AST units produced by mapping runs. */
final class AdaptedCodeMerger {

  /**
   * Caches platform-class lookups used while repairing imports. A cached {@code true} means the
   * fully qualified name resolves, without class initialization, to a public top-level JDK type;
   * such a type is already available through {@code java.lang} or a matching wildcard import.
   */
  private static final Map<String, Boolean> PLATFORM_PUBLIC_TOP_LEVEL_TYPE_CACHE =
      new ConcurrentHashMap<>();

  /**
   * Merges newly adapted units into output accumulated from earlier mapping passes.
   *
   * <p>Units are matched by package-qualified top-level identity, not by source-file path. Thus two
   * passes producing {@code shipping.Port} contribute members to one unit, while {@code api.Port}
   * remains separate.
   */
  Set<ASTOrdinaryCompilationUnit> mergeAdaptedCode(
      Set<ASTOrdinaryCompilationUnit> actualCode,
      Set<ASTOrdinaryCompilationUnit> newAdaptedCode) {
    Map<String, ASTOrdinaryCompilationUnit> merged = indexAndMerge(actualCode);
    for (ASTOrdinaryCompilationUnit newAdapted : ordered(newAdaptedCode)) {
      String key = compilationUnitKey(newAdapted);
        merged.compute(
          key, (k, existing) -> existing == null ? newAdapted : AdapterUtils.mergeAsts(existing, newAdapted));
    }
    return new LinkedHashSet<>(merged.values());
  }

  /**
   * Retains compilation units relevant to one mapping-specific incarnation context.
   *
   * <p>A unit is retained when a top-level Java type matches a reference-CD type having at least
   * one incarnation in {@code context}. A unit explicitly marked {@code @Adapt(ignore = true)} is
   * also retained: ignore prevents transformation of that type, but does not mean that its source
   * file should disappear from the mapping output.
   */
  Set<ASTOrdinaryCompilationUnit> filterCodeForMapping(
      Set<ASTOrdinaryCompilationUnit> javaFiles,
      CodeValidator validator,
      IncarnationContext context,
      CDModelIndex referenceIndex) {
    validator.initializeTypeMatcher(javaFiles);
    Set<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : javaFiles) {
      JavaAstElemCollector collector = collect(unit);

      boolean mappedTopLevel =
          collector.getAllTypeDeclarations().stream()
              .map(validator::getMatchedType)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .flatMap(matching -> matching.getReferences().stream())
              .anyMatch(
                  reference -> StableElementKey.fromSymbol(reference, referenceIndex)
                      .map(context::getIncarnations)
                      .filter(incarnations -> !incarnations.isEmpty())
                      .isPresent());
      boolean ignoredTopLevel =
          collector.getAllTypeDeclarations().stream()
              .map(validator::getMatchedType)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .anyMatch(
                  matching ->
                      !matching.mustBePerform() && matching.isExplicitAnnotation());
      if (mappedTopLevel || ignoredTopLevel) {
        result.add(unit);
      }
    }
    return result;
  }

  /**
   * Builds the final generated code on top of concrete handwritten classes. Adapted and concrete
   * units with the same package-qualified top-level identity are merged.
   *
   * <p>For example, an adapted {@code adapter.Port} containing {@code void send()} can be relocated
   * to a uniquely matching handwritten {@code concrete.Port}; the result remains in package {@code
   * concrete} and contains {@code send()}.
   */
  Set<ASTOrdinaryCompilationUnit> mergeAdaptedCodeIntoConcreteBase(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {

    Map<String, ASTOrdinaryCompilationUnit> result = indexAndMerge(concreteCode);
    Map<String, ASTOrdinaryCompilationUnit> mergedAdapted = indexAndMerge(adaptedCode);
    Map<String, Relocation> relocations = findRelocations(result, mergedAdapted, concreteIndex);
    Map<String, Map<String, String>> importsByOriginalPackage =
        adaptedTypeImports(mergedAdapted, relocations);
    Map<String, Set<String>> repairableMemberTypesByUnit = new LinkedHashMap<>();

    // Every unit in adaptedCode has already passed filterCodeForMapping. Retaining that explicit
    // selection avoids guessing from coincidental type-name prefixes or suffixes.
    for (ASTOrdinaryCompilationUnit originalAdapted : mergedAdapted.values()) {
      String originalKey = compilationUnitKey(originalAdapted);
      ASTOrdinaryCompilationUnit adaptedUnit = originalAdapted.deepClone();
      Relocation relocation = relocations.get(originalKey);
      addRelocationImports(
          adaptedUnit,
          importsByOriginalPackage.getOrDefault(packageName(originalAdapted), Map.of()),
          relocation == null ? packageName(adaptedUnit) : relocation.targetPackage());

      String key = compilationUnitKey(adaptedUnit);
      ASTOrdinaryCompilationUnit concreteMatch = result.get(key);
      ASTOrdinaryCompilationUnit mergeableAdapted = adaptedUnit;
      if (concreteMatch == null && relocation != null) {
        key = relocation.targetKey();
        concreteMatch = result.get(key);
        if (concreteMatch.isPresentPackageDeclaration()) {
          mergeableAdapted.setPackageDeclaration(
              concreteMatch.getPackageDeclaration().deepClone());
        } else {
          mergeableAdapted.setPackageDeclarationAbsent();
        }
      }
      Set<String> repairableMemberTypes = unqualifiedMemberTypeNames(mergeableAdapted);
      if (concreteMatch != null) {
        // Imports affect every occurrence of a simple name in the compilation unit. If concrete
        // handwritten code already used that name, changing its binding would be unsafe even when
        // an adapted member also uses it.
        repairableMemberTypes.removeAll(unqualifiedMemberTypeNames(concreteMatch));
      }
      repairableMemberTypesByUnit
          .computeIfAbsent(key, ignored -> new LinkedHashSet<>())
          .addAll(repairableMemberTypes);
      result.put(
          key,
          concreteMatch == null
              ? mergeableAdapted
              : AdapterUtils.mergeAstsPreferringLeft(concreteMatch, mergeableAdapted));
    }

    repairMemberTypeImports(result, repairableMemberTypesByUnit);

    return new LinkedHashSet<>(result.values());
  }

  /**
   * Imports final concrete declaration locations used by generated fields, methods and
   * supertypes. Completion can introduce member types after the initial reference-CD import
   * projection, and relocation can place those types in packages different from their adapted
   * owner.
   */
  private void repairMemberTypeImports(
      Map<String, ASTOrdinaryCompilationUnit> units,
      Map<String, Set<String>> repairableMemberTypesByUnit) {
    Map<String, Set<String>> declarationsBySimpleName = new LinkedHashMap<>();
    for (ASTOrdinaryCompilationUnit unit : units.values()) {
      String declarationPackage = packageName(unit);
      for (ASTTypeDeclaration type : unit.getTypeDeclarationList()) {
        declarationsBySimpleName
            .computeIfAbsent(type.getName(), ignored -> new LinkedHashSet<>())
            .add(qualify(declarationPackage, type.getName()));
      }
    }

    for (Map.Entry<String, ASTOrdinaryCompilationUnit> unitEntry : units.entrySet()) {
      ASTOrdinaryCompilationUnit unit = unitEntry.getValue();
      Set<String> repairableMemberTypes =
          repairableMemberTypesByUnit.getOrDefault(unitEntry.getKey(), Set.of());
      if (repairableMemberTypes.isEmpty()) {
        continue;
      }
      Set<JavaSourceNames.TypeReferenceName> referencedTypes = memberTypeNames(unit);
      Set<String> declaredSimpleNames =
          unit.getTypeDeclarationList().stream()
              .map(ASTTypeDeclaration::getName)
              .collect(java.util.stream.Collectors.toSet());
      Set<String> existingImports =
          unit.getImportDeclarationList().stream()
              .filter(
                  importDeclaration ->
                      !importDeclaration.isStatic() && !importDeclaration.isSTAR())
              .map(importDeclaration -> importDeclaration.getMCQualifiedName().getQName())
              .filter(qualifiedName -> qualifiedName.contains("."))
              .collect(java.util.stream.Collectors.toSet());
      Map<String, String> explicitImportsBySimpleName = new LinkedHashMap<>();
      for (String existingImport : existingImports) {
        explicitImportsBySimpleName.put(simpleName(existingImport), existingImport);
      }
      Set<String> wildcardImportPackages =
          unit.getImportDeclarationList().stream()
              .filter(
                  importDeclaration ->
                      !importDeclaration.isStatic() && importDeclaration.isSTAR())
              .map(importDeclaration -> importDeclaration.getMCQualifiedName().getQName())
              .collect(java.util.stream.Collectors.toSet());
      Map<String, String> requiredImports = new LinkedHashMap<>();
      for (JavaSourceNames.TypeReferenceName reference : referencedTypes) {
        if (reference.qualified()) {
          continue;
        }
        String simpleName = reference.simpleName();
        if (!repairableMemberTypes.contains(simpleName)) {
          continue;
        }
        Set<String> candidates = declarationsBySimpleName.getOrDefault(simpleName, Set.of());
        if (declaredSimpleNames.contains(simpleName) || candidates.isEmpty()) {
          continue;
        }
        String explicitImport = explicitImportsBySimpleName.get(simpleName);
        if (explicitImport != null) {
          // A single-type import is an established binding. It may belong to handwritten code
          // that shares a compilation unit with generated members, so never reinterpret it from
          // coincidental declarations in the final output.
          continue;
        }
        boolean samePackageCandidate =
            candidates.stream()
                .anyMatch(candidate -> packageName(candidate).equals(packageName(unit)));
        if (samePackageCandidate) {
          continue;
        }
        if (isImplicitJavaLangType(simpleName)) {
          // The implicit java.lang binding has the same precedence as a pre-existing explicit
          // import for this purpose. Generated code must use a qualified name if it means another
          // type with the same simple name.
          continue;
        }
        List<String> matchingWildcardPackages =
            wildcardImportPackages.stream()
                .filter(
                    importedPackage ->
                        isAvailableThroughWildcardImport(
                            importedPackage, simpleName, candidates))
                .toList();
        if (matchingWildcardPackages.size() == 1) {
          // Unrelated declarations do not make the wildcard binding ambiguous: only declarations
          // from imported packages participate in that binding.
          continue;
        }
        if (matchingWildcardPackages.size() > 1) {
          throw new CodeAdaptationException(
              "Cannot repair generated member type '"
                  + simpleName
                  + "' because multiple wildcard imports provide final declarations: "
                  + matchingWildcardPackages);
        }
        if (candidates.size() == 1) {
          // A single-type import takes precedence over unrelated on-demand imports.
          requiredImports.put(simpleName, candidates.iterator().next());
        } else if (candidates.size() > 1) {
          throw new CodeAdaptationException(
              "Cannot import generated member type '"
                  + simpleName
                  + "' because final output contains multiple declarations: "
                  + candidates);
        }
      }
      addRelocationImports(unit, requiredImports, packageName(unit));
    }
  }

  /** Returns whether the running Java platform implicitly provides the simple name. */
  private boolean isImplicitJavaLangType(String simpleName) {
    if (simpleName == null || simpleName.isBlank() || simpleName.contains(".")) {
      return false;
    }
    return isLoadablePublicTopLevelType("java.lang." + simpleName);
  }

  /**
   * A wildcard import covers either a type produced in the final output or a public top-level JDK
   * type. Checking both avoids adding redundant explicit imports after relocation.
   */
  private boolean isAvailableThroughWildcardImport(
      String importedPackage, String simpleName, Set<String> outputCandidates) {
    String qualifiedName = qualify(importedPackage, simpleName);
    return outputCandidates.contains(qualifiedName)
        || isLoadablePublicTopLevelType(qualifiedName);
  }

  private boolean isLoadablePublicTopLevelType(String qualifiedName) {
    return PLATFORM_PUBLIC_TOP_LEVEL_TYPE_CACHE.computeIfAbsent(
        qualifiedName, AdaptedCodeMerger::loadPublicTopLevelType);
  }

  /**
   * Probes only the JDK platform class loader and does not initialize the class. Nested and
   * non-public classes are deliberately rejected because a Java wildcard import cannot expose
   * them as top-level source types.
   */
  private static boolean loadPublicTopLevelType(String qualifiedName) {
    try {
      Class<?> type =
          Class.forName(qualifiedName, false, ClassLoader.getPlatformClassLoader());
      return type.getEnclosingClass() == null
          && java.lang.reflect.Modifier.isPublic(type.getModifiers());
    } catch (ClassNotFoundException | LinkageError ignored) {
      return false;
    }
  }

  /** Collects unqualified signature names that originated in an adapted compilation unit. */
  private Set<String> unqualifiedMemberTypeNames(ASTOrdinaryCompilationUnit unit) {
    return memberTypeNames(unit).stream()
        .filter(reference -> !reference.qualified())
        .map(JavaSourceNames.TypeReferenceName::simpleName)
        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
  }

  /** Collects type names appearing in declaration signatures that may require final imports. */
  private Set<JavaSourceNames.TypeReferenceName> memberTypeNames(
      ASTOrdinaryCompilationUnit unit) {
    Set<JavaSourceNames.TypeReferenceName> names = new LinkedHashSet<>();
    JavaAstElemCollector collector = collect(unit);
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      collector.getAllFSuperTypeDeclarations(type).forEach(value -> collectTypeNames(value, names));
      collector
          .getAllFieldDeclarations(type)
          .forEach(field -> collectTypeNames(field.getMCType(), names));
      collector
          .getAllMethodDeclarations(type)
          .forEach(
              method -> {
                collectTypeNames(method.getMCReturnType(), names);
                collector
                    .getAllParameters(type, method)
                    .forEach(parameter -> collectTypeNames(parameter.getMCType(), names));
              });
    }
    return names;
  }

  /** Retains qualified identity while traversing arrays, generics, and wildcard bounds. */
  private void collectTypeNames(
      de.monticore.ast.ASTNode type, Set<JavaSourceNames.TypeReferenceName> names) {
    names.addAll(JavaSourceNames.typeReferences(JavaLoader.print(type)));
  }

  /**
   * Finds adapted units that must move to an existing concrete handwritten package before merge.
   *
   * <p>The result maps the adapted unit's original package-qualified identity to its concrete
   * target identity and package. Relocation requires one unique concrete Java declaration with the
   * same concrete-CD type name; zero matches leave the unit in place and multiple matches fail.
   */
  private Map<String, Relocation> findRelocations(
      Map<String, ASTOrdinaryCompilationUnit> concreteCode,
      Map<String, ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    Map<String, Relocation> relocations = new LinkedHashMap<>();
    for (Map.Entry<String, ASTOrdinaryCompilationUnit> entry : adaptedCode.entrySet()) {
      if (concreteCode.containsKey(entry.getKey())) {
        continue;
      }
      Optional<Map.Entry<String, ASTOrdinaryCompilationUnit>> target =
          uniqueConcreteTypeMatch(concreteCode, entry.getValue(), concreteIndex);
      if (target.isEmpty()) {
        continue;
      }
      ASTOrdinaryCompilationUnit adaptedUnit = entry.getValue();
      ASTOrdinaryCompilationUnit concreteUnit = target.get().getValue();
      String simpleName = adaptedUnit.getTypeDeclarationList().get(0).getName();
      relocations.put(
          entry.getKey(),
          new Relocation(
              simpleName,
              target.get().getKey(),
              packageName(concreteUnit)));
    }
    return relocations;
  }

  /**
   * Indexes final adapted type locations for cross-package import repair.
   *
   * <p>The outer key is the type's original package. Each inner map is {@code simple type name ->
   * final qualified name}. For example, relocating {@code adapter.Port} to {@code concrete.Port}
   * records {@code adapter -> {Port=concrete.Port}}, allowing other units originally in {@code
   * adapter} to import the relocated type.
   */
  private Map<String, Map<String, String>> adaptedTypeImports(
      Map<String, ASTOrdinaryCompilationUnit> adaptedCode,
      Map<String, Relocation> relocations) {
    Map<String, Map<String, String>> result = new LinkedHashMap<>();
    for (Map.Entry<String, ASTOrdinaryCompilationUnit> entry : adaptedCode.entrySet()) {
      ASTOrdinaryCompilationUnit unit = entry.getValue();
      if (unit.getTypeDeclarationList().size() != 1) {
        continue;
      }
      String originalPackage = packageName(unit);
      String typeName = unit.getTypeDeclarationList().get(0).getName();
      Relocation relocation = relocations.get(entry.getKey());
      String qualifiedTarget =
          relocation == null
              ? qualify(originalPackage, typeName)
              : qualify(relocation.targetPackage(), typeName);
      Map<String, String> bySimpleName =
          result.computeIfAbsent(originalPackage, ignored -> new LinkedHashMap<>());
      String conflict = bySimpleName.putIfAbsent(typeName, qualifiedTarget);
      if (conflict != null && !conflict.equals(qualifiedTarget)) {
        throw new CodeAdaptationException(
            "Ambiguous concrete package for adapted type '"
                + typeName
                + "': "
                + conflict
                + " and "
                + qualifiedTarget);
      }
    }
    return result;
  }

  private void addRelocationImports(
      ASTOrdinaryCompilationUnit unit,
      Map<String, String> relocatedTypes,
      String finalPackage) {
    Set<String> declaredTypes =
        unit.getTypeDeclarationList().stream()
            .map(ASTTypeDeclaration::getName)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    Map<String, String> importsBySimpleName = new LinkedHashMap<>();
    for (ASTImportDeclaration existingImport :
        new ArrayList<>(unit.getImportDeclarationList())) {
      String qualifiedName = existingImport.getMCQualifiedName().getQName();
      String importedSimpleName = simpleName(qualifiedName);
      if (!qualifiedName.contains(".") && relocatedTypes.containsKey(importedSimpleName)) {
        unit.removeImportDeclaration(existingImport);
      } else {
        importsBySimpleName.put(importedSimpleName, qualifiedName);
      }
    }
    for (Map.Entry<String, String> relocatedType : relocatedTypes.entrySet()) {
      String simpleName = relocatedType.getKey();
      String qualifiedName = relocatedType.getValue();
      if (declaredTypes.contains(simpleName) || packageName(qualifiedName).equals(finalPackage)) {
        continue;
      }
      String conflict = importsBySimpleName.putIfAbsent(simpleName, qualifiedName);
      if (conflict != null && !conflict.equals(qualifiedName)) {
        throw new CodeAdaptationException(
            "Cannot import relocated type '"
                + qualifiedName
                + "' because '"
                + conflict
                + "' already uses the simple name '"
                + simpleName
                + "'");
      }
      if (conflict == null) {
        unit.addImportDeclaration(parseImport(qualifiedName));
      }
    }
  }

  private ASTImportDeclaration parseImport(String qualifiedName) {
    try {
      Optional<ASTCompilationUnit> parsed =
          JavaDSLMill.parser()
              .parse_StringCompilationUnit(
                  "import " + qualifiedName + ";" + System.lineSeparator() + "class Holder {}");
      if (parsed.isEmpty() || !(parsed.get() instanceof ASTOrdinaryCompilationUnit unit)) {
        throw new CodeAdaptationException("Could not create Java import for " + qualifiedName);
      }
      return unit.getImportDeclaration(0).deepClone();
    } catch (IOException exception) {
      throw new CodeAdaptationException("Could not create Java import for " + qualifiedName, exception);
    }
  }

  private Optional<Map.Entry<String, ASTOrdinaryCompilationUnit>> uniqueConcreteTypeMatch(
      Map<String, ASTOrdinaryCompilationUnit> concreteCode,
      ASTOrdinaryCompilationUnit adaptedUnit,
      CDModelIndex concreteIndex) {
    if (adaptedUnit.getTypeDeclarationList().size() != 1) {
      return Optional.empty();
    }
    String adaptedType = adaptedUnit.getTypeDeclarationList().get(0).getName();
    boolean isConcreteType = concreteIndex.hasType(adaptedType);
    if (!isConcreteType) {
      return Optional.empty();
    }
    List<Map.Entry<String, ASTOrdinaryCompilationUnit>> candidates =
        concreteCode.entrySet().stream()
            .filter(
                entry ->
                    entry.getValue().getTypeDeclarationList().stream()
                        .anyMatch(type -> adaptedType.equals(type.getName())))
            .toList();
    if (candidates.size() > 1) {
      throw new CodeAdaptationException(
          "Cannot relocate adapted type '"
              + adaptedType
              + "' because concrete Java declares it in multiple packages: "
              + candidates.stream().map(Map.Entry::getKey).sorted().toList());
    }
    return candidates.size() == 1 ? Optional.of(candidates.get(0)) : Optional.empty();
  }

  Set<ASTOrdinaryCompilationUnit> splitCompilationUnitsByType(
      Set<ASTOrdinaryCompilationUnit> units) {
    Set<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : ordered(units)) {
      List<ASTTypeDeclaration> types = new ArrayList<>(unit.getTypeDeclarationList());
      if (types.size() <= 1) {
        result.add(unit);
        continue;
      }

      for (ASTTypeDeclaration type : types) {
        ASTOrdinaryCompilationUnit copy = unit.deepClone();
        for (ASTTypeDeclaration copiedType : new ArrayList<>(copy.getTypeDeclarationList())) {
          if (!copiedType.getName().equals(type.getName())) {
            copy.removeTypeDeclaration(copiedType);
          }
        }

        String fileName = type.getName() + ".java";
        copy.get_SourcePositionStart().setFileName(fileName);
        result.add(copy);
      }
    }
    return result;
  }

  /**
   * Indexes units by package-qualified top-level identity and merges duplicate identities.
   * Source paths do not participate in the key.
   */
  private Map<String, ASTOrdinaryCompilationUnit> indexAndMerge(
      Set<ASTOrdinaryCompilationUnit> files) {
    Map<String, ASTOrdinaryCompilationUnit> byQualifiedType = new LinkedHashMap<>();
    for (ASTOrdinaryCompilationUnit file : ordered(files)) {
      String key = compilationUnitKey(file);
      ASTOrdinaryCompilationUnit existing = byQualifiedType.get(key);
      if (existing == null) {
        byQualifiedType.put(key, file);
      } else {
        byQualifiedType.put(key, AdapterUtils.mergeAsts(existing, file));
      }
    }
    return byQualifiedType;
  }

  private List<ASTOrdinaryCompilationUnit> ordered(Set<ASTOrdinaryCompilationUnit> files) {
    return files.stream()
        .sorted(
            Comparator.comparing(this::compilationUnitKey)
                .thenComparing(AdapterUtils::getFileName))
        .toList();
  }

  /** Returns the package-qualified identity of a split compilation unit. */
  private String compilationUnitKey(ASTOrdinaryCompilationUnit unit) {
    String packageName = packageName(unit);
    String typeIdentity =
        unit.getTypeDeclarationList().stream()
            .map(ASTTypeDeclaration::getName)
            .sorted()
            .reduce((left, right) -> left + "+" + right)
            .orElseGet(() -> AdapterUtils.getSimpleFileName(unit));
    return packageName.isEmpty() ? typeIdentity : packageName + "." + typeIdentity;
  }

  private String packageName(ASTOrdinaryCompilationUnit unit) {
    return unit.isPresentPackageDeclaration()
        ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
        : "";
  }

  private String packageName(String qualifiedName) {
    int separator = qualifiedName.lastIndexOf('.');
    return separator < 0 ? "" : qualifiedName.substring(0, separator);
  }

  private String simpleName(String qualifiedName) {
    int separator = qualifiedName.lastIndexOf('.');
    return separator < 0 ? qualifiedName : qualifiedName.substring(separator + 1);
  }

  private String qualify(String packageName, String simpleName) {
    return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
  }

  private JavaAstElemCollector collect(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }

  private record Relocation(String simpleName, String targetKey, String targetPackage) {}
}
