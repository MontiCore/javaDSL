package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.symboltable.ISymbol;
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
      IncarnationContext context) {
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
                  reference -> {
                    List<ISymbol> incarnations = context.getIncarnations(reference);
                    return incarnations != null && !incarnations.isEmpty();
                  });
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

    // Every unit in adaptedCode has already passed filterCodeForMapping. Retaining that explicit
    // selection avoids guessing from coincidental type-name prefixes or suffixes.
    for (ASTOrdinaryCompilationUnit adaptedUnit : indexAndMerge(adaptedCode).values()) {
      String key = compilationUnitKey(adaptedUnit);
      ASTOrdinaryCompilationUnit concreteMatch = result.get(key);
      ASTOrdinaryCompilationUnit mergeableAdapted = adaptedUnit;
      if (concreteMatch == null) {
        Optional<Map.Entry<String, ASTOrdinaryCompilationUnit>> uniqueConcreteMatch =
            uniqueConcreteTypeMatch(result, adaptedUnit, conCD);
        if (uniqueConcreteMatch.isPresent()) {
          key = uniqueConcreteMatch.get().getKey();
          concreteMatch = uniqueConcreteMatch.get().getValue();
          mergeableAdapted = adaptedUnit.deepClone();
          if (concreteMatch.isPresentPackageDeclaration()) {
            mergeableAdapted.setPackageDeclaration(
                concreteMatch.getPackageDeclaration().deepClone());
          } else {
            mergeableAdapted.setPackageDeclarationAbsent();
          }
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
    String packageName =
        unit.isPresentPackageDeclaration()
            ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
            : "";
    String typeIdentity =
        unit.getTypeDeclarationList().stream()
            .map(ASTTypeDeclaration::getName)
            .sorted()
            .reduce((left, right) -> left + "+" + right)
            .orElseGet(() -> AdapterUtils.getSimpleFileName(unit));
    return packageName.isEmpty() ? typeIdentity : packageName + "." + typeIdentity;
  }

  private JavaAstElemCollector collect(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }
}
