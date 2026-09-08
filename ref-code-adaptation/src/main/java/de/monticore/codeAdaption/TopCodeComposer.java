package de.monticore.codeAdaption;

import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTConstructorDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Keeps adapted implementations separate from concrete handwritten code through TOP inheritance. */
final class TopCodeComposer {

  private final AdaptedCodeMerger codeMerger;

  TopCodeComposer(AdaptedCodeMerger codeMerger) {
    this.codeMerger = codeMerger;
  }

  /**
   * Produces one output set containing unchanged concrete HWC and separate adapted TOP companions.
   * Analysis completes before either cloned input set is mutated, so invalid inheritance or name
   * collisions fail deterministically.
   */
  Set<ASTOrdinaryCompilationUnit> compose(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    return composeWithSelfTypeBindings(concreteCode, adaptedCode, concreteIndex).code();
  }

  /**
   * Composes the sources and records which generated TOP implementations represent a public HWC
   * subtype. The binding is used later to repair explicit {@code this} values in fluent APIs.
   */
  CompositionResult composeWithSelfTypeBindings(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    Set<ASTOrdinaryCompilationUnit> concreteCopies = cloneUnits(concreteCode);
    Set<ASTOrdinaryCompilationUnit> relocatedAdapted =
        codeMerger.relocateAdaptedCodeToConcretePackages(
            concreteCopies, cloneUnits(adaptedCode), concreteIndex);

    List<CompositionEntry> plan =
        analyze(concreteCopies, relocatedAdapted, concreteIndex);
    plan.forEach(this::apply);

    LinkedHashSet<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>();
    ordered(concreteCopies).forEach(result::add);
    ordered(relocatedAdapted).forEach(result::add);
    assertUniqueOutputTypes(result);
    Map<String, String> selfTypeBindings = new LinkedHashMap<>();
    for (CompositionEntry entry : plan) {
      String targetPackage = packageName(entry.concreteUnit());
      selfTypeBindings.put(
          qualify(targetPackage, entry.topName()),
          qualify(targetPackage, entry.concreteName()));
    }
    return new CompositionResult(result, Map.copyOf(selfTypeBindings));
  }

  private List<CompositionEntry> analyze(
      Set<ASTOrdinaryCompilationUnit> concreteCode,
      Set<ASTOrdinaryCompilationUnit> adaptedCode,
      CDModelIndex concreteIndex) {
    Map<String, TypeLocation> concreteByQualifiedName = indexTypes(concreteCode, "concrete HWC");
    Map<String, List<TypeLocation>> concreteBySimpleName = indexBySimpleName(concreteByQualifiedName);
    List<CompositionEntry> plan = new ArrayList<>();
    Map<String, String> finalIdentities = new LinkedHashMap<>();

    concreteByQualifiedName.keySet().stream()
        .sorted()
        .forEach(identity -> reserve(finalIdentities, identity, "concrete HWC"));

    for (ASTOrdinaryCompilationUnit adaptedUnit : ordered(adaptedCode)) {
      if (adaptedUnit.getTypeDeclarationList().size() != 1) {
        throw new CodeAdaptationException(
            "TOP composition requires one top-level declaration per adapted compilation unit: "
                + AdapterUtils.getFileName(adaptedUnit));
      }
      ASTTypeDeclaration adaptedType = adaptedUnit.getTypeDeclaration(0);
      String concreteName = adaptedType.getName();
      String adaptedIdentity = qualify(packageName(adaptedUnit), concreteName);
      TypeLocation concrete =
          matchingConcreteType(
              adaptedIdentity, concreteName, concreteByQualifiedName, concreteBySimpleName,
              concreteIndex);

      if (concrete == null) {
        reserve(finalIdentities, adaptedIdentity, "adapted code");
        continue;
      }

      validateKinds(concreteName, adaptedType, concrete.type());
      validateConcreteInheritance(concreteName, concrete);
      validateAdaptedBase(concreteName, adaptedType);

      String topName = concreteName + "TOP";
      String topIdentity = qualify(packageName(concrete.unit()), topName);
      reserve(finalIdentities, topIdentity, "adapted TOP code");
      plan.add(
          new CompositionEntry(
              adaptedUnit,
              adaptedType,
              concrete.unit(),
              concrete.type(),
              concreteName,
              topName));
    }
    return List.copyOf(plan);
  }

  private TypeLocation matchingConcreteType(
      String adaptedIdentity,
      String concreteName,
      Map<String, TypeLocation> concreteByQualifiedName,
      Map<String, List<TypeLocation>> concreteBySimpleName,
      CDModelIndex concreteIndex) {
    TypeLocation exact = concreteByQualifiedName.get(adaptedIdentity);
    if (exact != null) {
      return exact;
    }
    // Generated pattern types such as PersonBuilder need not be declared in the concrete CD. The
    // CD is required only before falling back from a qualified identity to a simple-name match.
    if (!concreteIndex.hasType(concreteName)) {
      return null;
    }
    List<TypeLocation> candidates = concreteBySimpleName.getOrDefault(concreteName, List.of());
    if (candidates.size() > 1) {
      throw new CodeAdaptationException(
          "Cannot select concrete HWC for '"
              + concreteName
              + "' because it is declared in several packages: "
              + candidates.stream()
                  .map(candidate -> qualify(packageName(candidate.unit()), concreteName))
                  .sorted()
                  .toList());
    }
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  private void validateKinds(
      String concreteName, ASTTypeDeclaration adaptedType, ASTTypeDeclaration concreteType) {
    if (adaptedType instanceof ASTEnumDeclaration || concreteType instanceof ASTEnumDeclaration) {
      throw new CodeAdaptationException(
          "TOP separation is not supported for enum '" + concreteName + "'");
    }
    if (!adaptedType.getClass().equals(concreteType.getClass())) {
      throw new CodeAdaptationException(
          "Cannot TOP-compose '" + concreteName + "' because Java declaration kinds differ");
    }
  }

  private void validateConcreteInheritance(String concreteName, TypeLocation concrete) {
    ASTTypeDeclaration concreteType = concrete.type();
    if (!(concreteType instanceof ASTClassDeclaration concreteClass)
        || !concreteClass.isPresentSuperClass()) {
      return;
    }
    String expectedSimpleName = concreteName + "TOP";
    String expectedIdentity = qualify(packageName(concrete.unit()), expectedSimpleName);
    if (!isExpectedTopType(
        concrete.unit(), concreteClass.getSuperClass(), expectedSimpleName, expectedIdentity)) {
      throw new CodeAdaptationException(
          "Concrete HWC class '"
              + concreteName
              + "' already extends unrelated class '"
              + JavaSourceNames.printQualifiedType(concreteClass.getSuperClass())
              + "'");
    }
  }

  private boolean isExpectedTopType(
      ASTOrdinaryCompilationUnit unit,
      ASTMCType type,
      String expectedSimpleName,
    String expectedIdentity) {
    String printedType = JavaSourceNames.printQualifiedType(type);
    List<JavaSourceNames.TypeReferenceName> references =
        JavaSourceNames.typeReferences(printedType);
    String identity = references.isEmpty() ? printedType : references.get(0).originalName();
    return JavaSourceNames.simpleName(identity).equals(expectedSimpleName)
        && (!identity.contains(".") || identity.equals(expectedIdentity))
        && !importsDifferentTop(unit, expectedSimpleName, expectedIdentity);
  }

  /** Rejects an unqualified TOP name when an explicit import binds it to another package. */
  private boolean importsDifferentTop(
      ASTOrdinaryCompilationUnit unit, String simpleName, String expectedIdentity) {
    return unit.getImportDeclarationList().stream()
        .filter(importDeclaration -> !importDeclaration.isStatic() && !importDeclaration.isSTAR())
        .map(importDeclaration -> importDeclaration.getMCQualifiedName().getQName())
        .filter(importedName -> JavaSourceNames.simpleName(importedName).equals(simpleName))
        .anyMatch(importedName -> !importedName.equals(expectedIdentity));
  }

  private void validateAdaptedBase(String concreteName, ASTTypeDeclaration adaptedType) {
    if (adaptedType instanceof ASTClassDeclaration adaptedClass
        && adaptedClass.getJavaModifierList().stream()
            .map(JavaLoader::print)
            .map(String::trim)
            .anyMatch("final"::equals)) {
      throw new CodeAdaptationException(
          "Adapted class '" + concreteName + "' is final and cannot become a TOP base class");
    }
  }

  private void apply(CompositionEntry entry) {
    addTopParent(entry.concreteType(), entry.topName());
    shiftSelfTopParent(entry.adaptedUnit(), entry.adaptedType(), entry.topName());

    // A global type refactoring would also rewrite fields, return types, and `new Concrete()`.
    // Only the implementation declaration and its constructors belong to the TOP companion.
    entry.adaptedType().setName(entry.topName());
    if (entry.adaptedType() instanceof ASTClassDeclaration adaptedClass) {
      adaptedClass.getClassBody().getClassBodyDeclarationList().stream()
          .filter(ASTConstructorDeclaration.class::isInstance)
          .map(ASTConstructorDeclaration.class::cast)
          .forEach(constructor -> constructor.setName(entry.topName()));
    }
    entry.adaptedUnit().get_SourcePositionStart().setFileName(entry.topName() + ".java");
  }

  private void addTopParent(ASTTypeDeclaration concreteType, String topName) {
    if (concreteType instanceof ASTClassDeclaration concreteClass) {
      if (!concreteClass.isPresentSuperClass()) {
        concreteClass.setSuperClass(typeReference(topName));
      }
      return;
    }
    ASTInterfaceDeclaration concreteInterface = (ASTInterfaceDeclaration) concreteType;
    boolean alreadyPresent =
        concreteInterface.getExtendedInterfaceList().stream()
            .anyMatch(type -> simpleTypeName(type).equals(topName));
    if (!alreadyPresent) {
      concreteInterface.getExtendedInterfaceList().add(typeReference(topName));
    }
  }

  private void shiftSelfTopParent(
      ASTOrdinaryCompilationUnit adaptedUnit, ASTTypeDeclaration adaptedType, String topName) {
    String topTopName = topName + "TOP";
    String expectedIdentity = qualify(packageName(adaptedUnit), topName);
    if (adaptedType instanceof ASTClassDeclaration adaptedClass) {
      if (adaptedClass.isPresentSuperClass()
          && isExpectedTopType(
              adaptedUnit, adaptedClass.getSuperClass(), topName, expectedIdentity)) {
        adaptedClass.setSuperClass(typeReference(topTopName));
      }
      return;
    }
    ASTInterfaceDeclaration adaptedInterface = (ASTInterfaceDeclaration) adaptedType;
    List<ASTMCType> parents = adaptedInterface.getExtendedInterfaceList();
    for (int index = 0; index < parents.size(); index++) {
      if (isExpectedTopType(adaptedUnit, parents.get(index), topName, expectedIdentity)) {
        parents.set(index, typeReference(topTopName));
      }
    }
  }

  private ASTMCType typeReference(String name) {
    try {
      ASTCompilationUnit parsed =
          JavaDSLMill.parser()
              .parse_StringCompilationUnit("class Holder extends " + name + " {}")
              .orElseThrow(
                  () -> new CodeAdaptationException("Could not create Java type '" + name + "'"));
      ASTOrdinaryCompilationUnit unit = (ASTOrdinaryCompilationUnit) parsed;
      return ((ASTClassDeclaration) unit.getTypeDeclaration(0)).getSuperClass().deepClone();
    } catch (IOException exception) {
      throw new CodeAdaptationException("Could not create Java type '" + name + "'", exception);
    }
  }

  private Map<String, TypeLocation> indexTypes(
      Set<ASTOrdinaryCompilationUnit> units, String sourceLabel) {
    Map<String, TypeLocation> result = new LinkedHashMap<>();
    for (ASTOrdinaryCompilationUnit unit : ordered(units)) {
      for (ASTTypeDeclaration type : unit.getTypeDeclarationList()) {
        String identity = qualify(packageName(unit), type.getName());
        if (result.putIfAbsent(identity, new TypeLocation(unit, type)) != null) {
          throw new CodeAdaptationException(
              "Duplicate " + sourceLabel + " declaration '" + identity + "'");
        }
      }
    }
    return result;
  }

  private Map<String, List<TypeLocation>> indexBySimpleName(
      Map<String, TypeLocation> byQualifiedName) {
    Map<String, List<TypeLocation>> result = new LinkedHashMap<>();
    byQualifiedName.values().forEach(
        location ->
            result.computeIfAbsent(location.type().getName(), ignored -> new ArrayList<>())
                .add(location));
    return result;
  }

  private void assertUniqueOutputTypes(Set<ASTOrdinaryCompilationUnit> units) {
    indexTypes(units, "output");
  }

  private void reserve(Map<String, String> identities, String identity, String owner) {
    String conflict = identities.putIfAbsent(identity, owner);
    if (conflict != null) {
      throw new CodeAdaptationException(
          "Cannot produce '" + identity + "' because it is already provided by " + conflict);
    }
  }

  private Set<ASTOrdinaryCompilationUnit> cloneUnits(Set<ASTOrdinaryCompilationUnit> units) {
    LinkedHashSet<ASTOrdinaryCompilationUnit> result = new LinkedHashSet<>();
    ordered(units).forEach(unit -> result.add(unit.deepClone()));
    return result;
  }

  private List<ASTOrdinaryCompilationUnit> ordered(Set<ASTOrdinaryCompilationUnit> units) {
    return units.stream()
        .sorted(
            Comparator.comparing(this::unitIdentity)
                .thenComparing(AdapterUtils::getFileName))
        .toList();
  }

  private String unitIdentity(ASTOrdinaryCompilationUnit unit) {
    return packageName(unit)
        + ":"
        + unit.getTypeDeclarationList().stream()
            .map(ASTTypeDeclaration::getName)
            .sorted()
            .reduce((left, right) -> left + "+" + right)
            .orElse("");
  }

  private String packageName(ASTOrdinaryCompilationUnit unit) {
    return unit.isPresentPackageDeclaration()
        ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
        : "";
  }

  private String qualify(String packageName, String simpleName) {
    return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
  }

  private String simpleTypeName(ASTMCType type) {
    return JavaSourceNames.simpleName(JavaSourceNames.printNormalizedType(type));
  }

  private record TypeLocation(ASTOrdinaryCompilationUnit unit, ASTTypeDeclaration type) {}

  record CompositionResult(
      Set<ASTOrdinaryCompilationUnit> code, Map<String, String> topToPublicSelfTypes) {}

  private record CompositionEntry(
      ASTOrdinaryCompilationUnit adaptedUnit,
      ASTTypeDeclaration adaptedType,
      ASTOrdinaryCompilationUnit concreteUnit,
      ASTTypeDeclaration concreteType,
      String concreteName,
      String topName) {}
}
