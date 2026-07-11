package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDModelIndex;
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

/** Merges, filters, splits, and deduplicates Java AST units produced by mapping runs. */
final class AdaptedCodeMerger {

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
   */
  Set<ASTOrdinaryCompilationUnit> mergeAdaptedCodeIntoConcreteBase(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      ASTCDCompilationUnit conCD) {

    Map<String, ASTOrdinaryCompilationUnit> result = indexAndMerge(concreteCode);
    Map<String, ASTOrdinaryCompilationUnit> mergedAdapted = indexAndMerge(adaptedCode);
    Map<String, Relocation> relocations = findRelocations(result, mergedAdapted, conCD);
    Map<String, Map<String, String>> importsByOriginalPackage =
        adaptedTypeImports(mergedAdapted, relocations);

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
      result.put(
          key,
          concreteMatch == null
              ? mergeableAdapted
              : AdapterUtils.mergeAstsPreferringLeft(concreteMatch, mergeableAdapted));
    }

    return new LinkedHashSet<>(result.values());
  }

  private Map<String, Relocation> findRelocations(
      Map<String, ASTOrdinaryCompilationUnit> concreteCode,
      Map<String, ASTOrdinaryCompilationUnit> adaptedCode,
      ASTCDCompilationUnit conCD) {
    Map<String, Relocation> relocations = new LinkedHashMap<>();
    for (Map.Entry<String, ASTOrdinaryCompilationUnit> entry : adaptedCode.entrySet()) {
      if (concreteCode.containsKey(entry.getKey())) {
        continue;
      }
      Optional<Map.Entry<String, ASTOrdinaryCompilationUnit>> target =
          uniqueConcreteTypeMatch(concreteCode, entry.getValue(), conCD);
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
      ASTCDCompilationUnit conCD) {
    if (adaptedUnit.getTypeDeclarationList().size() != 1) {
      return Optional.empty();
    }
    String adaptedType = adaptedUnit.getTypeDeclarationList().get(0).getName();
    boolean isConcreteType = CDModelIndex.of(conCD).hasType(adaptedType);
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
