package de.monticore.codeAdaption;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
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
    if (actualCode.isEmpty()) {
      return deduplicateBySimpleName(newAdaptedCode);
    }

    Set<ASTOrdinaryCompilationUnit> deduplicatedNew = deduplicateBySimpleName(newAdaptedCode);

    for (ASTOrdinaryCompilationUnit newAdapted : deduplicatedNew) {
      String newSimpleName = AdapterUtils.getSimpleFileName(newAdapted);

      Optional<ASTOrdinaryCompilationUnit> actual =
          actualCode.stream()
              .filter(file -> AdapterUtils.getSimpleFileName(file).equals(newSimpleName))
              .findAny();

      if (actual.isEmpty()) {
        actualCode.add(newAdapted);
      } else {
        actualCode.remove(actual.get());
        actualCode.add(AdapterUtils.mergeAsts(actual.get(), newAdapted));
      }
    }
    return actualCode;
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
              .anyMatch(matching -> !matching.mustBePerform());
      if (mappedTopLevel || ignoredTopLevel) {
        result.add(unit);
      }
    }
    return result;
  }

  /**
   * Builds the final generated code on top of concrete handwritten classes. Adapted pattern types
   * with the same simple file name are merged into the concrete class, while reference-only
   * template artifacts such are filtered out.
   */
  Set<ASTOrdinaryCompilationUnit> mergeAdaptedCodeIntoConcreteBase(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      ASTCDCompilationUnit conCD) {

    Set<String> concreteTypeNames =
        CDModelIndex.of(conCD).types().stream()
            .map(ASTCDType::getName)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

    Set<ASTOrdinaryCompilationUnit> result =
        new LinkedHashSet<>(deduplicateBySimpleName(concreteCode));

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

  Set<ASTOrdinaryCompilationUnit> splitCompilationUnitsByType(
      Set<ASTOrdinaryCompilationUnit> units) {
    Set<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit unit : units) {
      JavaAstElemCollector collector = collect(unit);
      List<ASTTypeDeclaration> types = new ArrayList<>(collector.getAllTypeDeclarations());
      if (types.size() <= 1) {
        result.add(unit);
        continue;
      }

      for (ASTTypeDeclaration type : types) {
        ASTOrdinaryCompilationUnit copy = unit.deepClone();
        JavaAstElemCollector copyCollector = collect(copy);
        for (ASTTypeDeclaration copiedType : copyCollector.getAllTypeDeclarations()) {
          if (!copiedType.getName().equals(type.getName())) {
            copy.removeTypeDeclaration(copiedType);
          }
        }

        String fileName = type.getName() + ".java";
        copy.get_SourcePositionStart().setFileName(fileName);
        Log.info("splitCompilationUnitsByType -> created unit: " + fileName, "CodeAdapter");
        result.add(copy);
      }
    }
    return result;
  }

  private boolean shouldKeepAdaptedUnit(String typeName, Set<String> concreteTypeNames) {
    if (typeName == null || typeName.isEmpty()) {
      return false;
    }
    if (concreteTypeNames.contains(typeName)) {
      return true;
    }
    return concreteTypeNames.stream()
        .anyMatch(
            concreteName -> typeName.startsWith(concreteName) || typeName.endsWith(concreteName));
  }

  private Optional<String> getPrimaryTypeName(ASTOrdinaryCompilationUnit unit) {
    return collect(unit).getAllTypeDeclarations().stream()
        .map(ASTTypeDeclaration::getName)
        .findFirst();
  }

  private Set<ASTOrdinaryCompilationUnit> deduplicateBySimpleName(
      Set<ASTOrdinaryCompilationUnit> files) {
    Map<String, ASTOrdinaryCompilationUnit> bySimpleName = new LinkedHashMap<>();

    for (ASTOrdinaryCompilationUnit file : files) {
      String simpleName = AdapterUtils.getSimpleFileName(file);
      String fullPath = AdapterUtils.getFileName(file);

      ASTOrdinaryCompilationUnit existing = bySimpleName.get(simpleName);
      if (existing == null) {
        bySimpleName.put(simpleName, file);
      } else {
        String existingPath = AdapterUtils.getFileName(existing);
        if (JavaSourceNames.pathDepth(fullPath) > JavaSourceNames.pathDepth(existingPath)) {
          bySimpleName.put(simpleName, file);
        }
      }
    }

    return new LinkedHashSet<>(bySimpleName.values());
  }

  private JavaAstElemCollector collect(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }
}
