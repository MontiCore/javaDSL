package de.monticore.codeAdaption.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumConstantDeclaration;
import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Structural oracle for Java-expressible elements materialized in adapter output. */
public final class GeneratedJavaStructureOracle {

  private GeneratedJavaStructureOracle() {}

  public static void assertMatches(
      Path originalConcreteCd, Path expectedOutputCd, List<Path> generatedJava) {
    CDModelIndex original = CDModelIndex.of(JavaLoader.parseCD(originalConcreteCd.toString()));
    CDModelIndex expected = CDModelIndex.of(JavaLoader.parseCD(expectedOutputCd.toString()));
    Map<String, List<GeneratedType>> generatedBySimpleName = new LinkedHashMap<>();
    for (Path source : generatedJava) {
      ASTOrdinaryCompilationUnit unit = JavaLoader.loadJava(source.toFile());
      JavaAstElemCollector collector = collect(unit);
      for (ASTTypeDeclaration type : unit.getTypeDeclarationList()) {
        generatedBySimpleName
            .computeIfAbsent(type.getName(), ignored -> new ArrayList<>())
            .add(GeneratedType.from(type, collector, source));
      }
    }

    for (ASTCDType expectedType : expected.types()) {
      ASTCDType originalType = original.type(expectedType.getName()).orElse(null);
      List<GeneratedType> candidates =
          generatedBySimpleName.getOrDefault(expectedType.getName(), List.of());
      if (candidates.isEmpty()) {
        // Types without handwritten/adapted Java are supplied later by CD4Code and are represented
        // by strict compilation stubs rather than adapter output. When a type is materialized here,
        // all completion deltas below are mandatory.
        continue;
      }
      if (candidates.size() > 1) {
        fail(
            "Expected type '"
                + expectedType.getName()
                + "' is ambiguous in generated Java: "
                + candidates.stream().map(GeneratedType::source).toList());
      }
      assertType(originalType, expectedType, candidates.get(0));
    }
  }

  private static void assertType(
      ASTCDType original, ASTCDType expected, GeneratedType actual) {
    String context = expected.getName() + " in " + actual.source();
    assertEquals(kind(expected), actual.kind(), "Wrong type kind for " + context);

    Map<String, String> originalFields = fields(original);
    Map<String, String> expectedFields = new LinkedHashMap<>();
    expected
        .getCDAttributeList()
        .forEach(
            field ->
                expectedFields.put(
                    field.getName(), JavaSourceNames.printNormalizedFieldType(field)));
    expectedFields.forEach(
        (name, type) ->
            {
              if (!type.equals(originalFields.get(name))) {
                assertEquals(
                    type,
                    actual.fields().get(name),
                    "Missing or mistyped completed field '" + name + "' on " + context);
              }
            });

    Map<String, String> originalMethods = methods(original);
    Map<String, String> expectedMethods = new LinkedHashMap<>();
    expected
        .getCDMethodList()
        .forEach(
            method ->
                expectedMethods.put(
                    JavaSourceNames.methodSignature(method),
                    JavaSourceNames.printNormalizedReturnType(method)));
    expectedMethods.forEach(
        (signature, returnType) ->
            {
              if (!returnType.equals(originalMethods.get(signature))) {
                assertEquals(
                    returnType,
                    actual.methods().get(signature),
                    "Missing or mistyped completed method '" + signature + "' on " + context);
              }
            });

    String originalSuperclass =
        original == null
            ? null
            : CDTypeRelations.firstSuperclassName(original)
                .map(JavaSourceNames::simpleName)
                .orElse(null);
    String expectedSuperclass =
        expected.getSuperclassList().size() == 1
            ? CDTypeRelations.firstSuperclassName(expected)
                .map(JavaSourceNames::simpleName)
                .orElse(null)
            : null;
    if (expected.getSuperclassList().size() <= 1
        && !java.util.Objects.equals(originalSuperclass, expectedSuperclass)) {
      assertEquals(expectedSuperclass, actual.superclass(), "Wrong completed superclass for " + context);
    }
    Set<String> originalInterfaces = interfaces(original);
    Set<String> expectedInterfaces =
        CDTypeRelations.interfaceNames(expected).stream()
            .map(JavaSourceNames::simpleName)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    Set<String> addedInterfaces = difference(expectedInterfaces, originalInterfaces);
    assertTrue(
        actual.interfaces().containsAll(addedInterfaces),
        () ->
            "Missing interfaces "
                + difference(addedInterfaces, actual.interfaces())
                + " on "
                + context);

    if (expected instanceof ASTCDEnum expectedEnum) {
      List<String> originalConstants =
          original instanceof ASTCDEnum originalEnum
              ? originalEnum.getCDEnumConstantList().stream()
                  .map(constant -> constant.getName())
                  .toList()
              : List.of();
      List<String> constants =
          expectedEnum.getCDEnumConstantList().stream().map(constant -> constant.getName()).toList();
      if (!constants.equals(originalConstants)) {
        assertEquals(constants, actual.enumConstants(), "Wrong completed enum constants for " + context);
      }
    }
  }

  private static Map<String, String> fields(ASTCDType type) {
    Map<String, String> result = new LinkedHashMap<>();
    if (type != null) {
      type.getCDAttributeList()
          .forEach(
              field ->
                  result.put(field.getName(), JavaSourceNames.printNormalizedFieldType(field)));
    }
    return result;
  }

  private static Map<String, String> methods(ASTCDType type) {
    Map<String, String> result = new LinkedHashMap<>();
    if (type != null) {
      type.getCDMethodList()
          .forEach(
              method ->
                  result.put(
                      JavaSourceNames.methodSignature(method),
                      JavaSourceNames.printNormalizedReturnType(method)));
    }
    return result;
  }

  private static Set<String> interfaces(ASTCDType type) {
    if (type == null) {
      return Set.of();
    }
    return CDTypeRelations.interfaceNames(type).stream()
        .map(JavaSourceNames::simpleName)
        .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
  }

  private static String kind(ASTCDType type) {
    if (type instanceof de.monticore.cdbasis._ast.ASTCDClass) {
      return "class";
    }
    if (type instanceof de.monticore.cdinterfaceandenum._ast.ASTCDInterface) {
      return "interface";
    }
    return "enum";
  }

  private static Set<String> difference(Set<String> expected, Set<String> actual) {
    Set<String> missing = new LinkedHashSet<>(expected);
    missing.removeAll(actual);
    return missing;
  }

  private static JavaAstElemCollector collect(ASTOrdinaryCompilationUnit unit) {
    JavaAstElemCollector collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    unit.accept(traverser);
    return collector;
  }

  private record GeneratedType(
      String kind,
      Map<String, String> fields,
      Map<String, String> methods,
      String superclass,
      Set<String> interfaces,
      List<String> enumConstants,
      Path source) {

    private static GeneratedType from(
        ASTTypeDeclaration type, JavaAstElemCollector collector, Path source) {
      Map<String, String> fields = new LinkedHashMap<>();
      collector
          .getAllFieldDeclarations(type)
          .forEach(
              field ->
                  field
                      .getVariableDeclaratorList()
                      .forEach(
                          variable ->
                              fields.put(
                                  variable.getDeclarator().getName(),
                                  JavaSourceNames.printNormalizedType(field.getMCType()))));

      Map<String, String> methods = new LinkedHashMap<>();
      for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {
        List<String> parameterTypes =
            collector.getAllParameters(type, method).stream()
                .map(parameter -> JavaSourceNames.printNormalizedType(parameter.getMCType()))
                .toList();
        methods.put(
            method.getName() + "(" + String.join(",", parameterTypes) + ")",
            JavaSourceNames.normalizeType(JavaLoader.print(method.getMCReturnType())));
      }

      String kind;
      String superclass = null;
      Set<String> interfaces = new LinkedHashSet<>();
      List<String> enumConstants = List.of();
      if (type instanceof ASTClassDeclaration classDeclaration) {
        kind = "class";
        if (classDeclaration.isPresentSuperClass()) {
          superclass = JavaSourceNames.simpleName(JavaLoader.print(classDeclaration.getSuperClass()));
        }
        classDeclaration.getImplementedInterfaceList().stream()
            .map(JavaLoader::print)
            .map(JavaSourceNames::simpleName)
            .forEach(interfaces::add);
      } else if (type instanceof ASTInterfaceDeclaration interfaceDeclaration) {
        kind = "interface";
        interfaceDeclaration.getExtendedInterfaceList().stream()
            .map(JavaLoader::print)
            .map(JavaSourceNames::simpleName)
            .forEach(interfaces::add);
      } else if (type instanceof ASTEnumDeclaration enumDeclaration) {
        kind = "enum";
        enumDeclaration.getImplementedInterfaceList().stream()
            .map(JavaLoader::print)
            .map(JavaSourceNames::simpleName)
            .forEach(interfaces::add);
        List<String> constants = new ArrayList<>();
        JavaDSLTraverser traverser = JavaDSLMill.traverser();
        traverser.add4JavaDSL(
            new JavaDSLVisitor2() {
              @Override
              public void visit(ASTEnumConstantDeclaration constant) {
                constants.add(constant.getName());
              }
            });
        enumDeclaration.accept(traverser);
        enumConstants = List.copyOf(constants);
      } else {
        assertFalse(true, "Unsupported generated Java type " + type.getClass().getName());
        throw new AssertionError();
      }
      return new GeneratedType(
          kind,
          Map.copyOf(fields),
          Map.copyOf(methods),
          superclass,
          Set.copyOf(interfaces),
          enumConstants,
          source);
    }
  }
}
