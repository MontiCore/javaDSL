package de.monticore.codeAdaption.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.monticore.ast.ASTNode;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import spoon.Launcher;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

/** Structural oracle for generated Java output of cdconcretization-derived cases. */
public final class GeneratedJavaOracle {

  private static final Map<String, String> EXPECTED_OUT_OVERRIDES =
      Map.ofEntries(
          Map.entry(
              "evaluation/builder/DataModelConc.cd", "evaluation/builder/BuilderAndMillOut.cd"),
          Map.entry(
              "evaluation/getter-setter/DataModelConc.cd", "evaluation/getter-setter/GetterOut.cd"),
          Map.entry(
              "evaluation/mill/LanguageInfrastructureConc.cd", "evaluation/mill/MillOut.cd"));

  private GeneratedJavaOracle() {}

  public static void assertMatchesExpectedStructure(
      CDConcretizationTestCase testCase,
      Set<CDConfParameter> confParameters,
      List<Path> generatedJavaFiles) {
    JavaModel expected = fromExpectedCD(testCase, confParameters);
    JavaModel actual = fromJavaFiles(generatedJavaFiles);

    assertEquals(
        expected.types().keySet(),
        actual.types().keySet(),
        () -> "Generated Java types differ from expected CD for " + testCase.displayName());

    for (String typeName : expected.types().keySet()) {
      JavaType expectedType = expected.types().get(typeName);
      JavaType actualType = actual.types().get(typeName);
      assertEquals(
          expectedType.kind(),
          actualType.kind(),
          () -> "Type kind differs for " + testCase.displayName() + " / " + typeName);
      if (expectedType.compareSuperclass()) {
        assertEquals(
            expectedType.superClass(),
            actualType.superClass(),
            () -> "Superclass differs for " + testCase.displayName() + " / " + typeName);
      }
      assertEquals(
          expectedType.interfaces(),
          actualType.interfaces(),
          () -> "Implemented/extended interfaces differ for "
              + testCase.displayName()
              + " / "
              + typeName);
      assertEquals(
          expectedType.enumConstants(),
          actualType.enumConstants(),
          () -> "Enum constants differ for " + testCase.displayName() + " / " + typeName);
      assertEquals(
          expectedType.fields(),
          actualType.fields(),
          () -> "Fields differ for " + testCase.displayName() + " / " + typeName);
      assertEquals(
          expectedType.methods(),
          actualType.methods(),
          () -> "Methods differ for " + testCase.displayName() + " / " + typeName);
    }
  }

  private static JavaModel fromExpectedCD(
      CDConcretizationTestCase testCase, Set<CDConfParameter> confParameters) {
    Optional<Path> expectedPath = resolveExpectedCD(testCase);
    ASTCDCompilationUnit expectedCD;
    if (expectedPath.isPresent()) {
      expectedCD = JavaLoader.parseCD(expectedPath.get().toString());
    } else {
      expectedCD = JavaLoader.parseCD(testCase.concCd().toString());
      ASTCDCompilationUnit refCD = JavaLoader.parseCD(testCase.refCd().toString());
      try {
        new ConcretizationCompleter(confParameters)
            .completeCD(expectedCD, refCD, new ArrayList<>(testCase.mappings()));
      } catch (Exception e) {
        // Some copied fixtures have no completed model-level expectation. For those cases the
        // unchanged concrete CD is the Java-expressible fallback expectation.
      }
    }
    return fromCD(expectedCD);
  }

  private static Optional<Path> resolveExpectedCD(CDConcretizationTestCase testCase) {
    String concName = testCase.concCd().getFileName().toString();
    String relativeConc =
        Path.of(CDConcretizationTestCase.RESOURCE_ROOT)
            .relativize(testCase.concCd())
            .toString()
            .replace('\\', '/');
    if (EXPECTED_OUT_OVERRIDES.containsKey(relativeConc)) {
      Path out = Path.of(CDConcretizationTestCase.RESOURCE_ROOT).resolve(EXPECTED_OUT_OVERRIDES.get(relativeConc));
      if (Files.exists(out)) {
        return Optional.of(out);
      }
    }
    String outName = concName.replace("Conc.cd", "Out.cd");
    Path out = testCase.concCd().resolveSibling(outName);
    if (Files.exists(out)) {
      return Optional.of(out);
    }
    return Optional.empty();
  }

  private static JavaModel fromCD(ASTCDCompilationUnit cd) {
    Map<String, JavaType> types = new LinkedHashMap<>();
    AdapterUtils.getAllCDTypes(cd).stream()
        .sorted(Comparator.comparing(ASTCDType::getName))
        .forEach(type -> types.put(type.getName(), fromCDType(type)));
    return new JavaModel(types);
  }

  private static JavaType fromCDType(ASTCDType type) {
    TypeKind kind = typeKind(type);
    Set<String> enumConstants = new LinkedHashSet<>();
    Map<String, String> fields = new LinkedHashMap<>();
    Set<MethodSignature> methods = new LinkedHashSet<>();

    if (type instanceof ASTCDEnum enumType) {
      enumType.getCDEnumConstantList().forEach(constant -> enumConstants.add(constant.getName()));
    } else if (!(type instanceof ASTCDInterface)) {
      type.getCDAttributeList().stream()
          .sorted(Comparator.comparing(ASTCDAttribute::getName))
          .forEach(attribute -> fields.put(attribute.getName(), normalizeType(printType(attribute.getMCType()))));
    }

    type.getCDMethodList().stream()
        .filter(method -> isJavaExpressibleMethod(type, method))
        .map(method -> fromCDMethod(type, method))
        .sorted()
        .forEach(methods::add);

    return new JavaType(
        kind,
        readSuperclass(type),
        compareSuperclass(type),
        new LinkedHashSet<>(readInterfaceNames(type)),
        enumConstants,
        fields,
        methods);
  }

  private static MethodSignature fromCDMethod(ASTCDType owner, ASTCDMethod method) {
    List<Parameter> parameters = new ArrayList<>();
    for (ASTCDParameter parameter : method.getCDParameterList()) {
      parameters.add(new Parameter(parameter.getName(), normalizeType(printType(parameter.getMCType()))));
    }
    return new MethodSignature(
        method.getName(),
        normalizeType(JavaLoader.print((ASTNode) method.getMCReturnType())),
        parameters);
  }

  private static JavaModel fromJavaFiles(List<Path> generatedJavaFiles) {
    Launcher launcher = new Launcher();
    launcher.getEnvironment().setNoClasspath(true);
    for (Path file : generatedJavaFiles) {
      launcher.addInputResource(file.toAbsolutePath().toString());
    }
    launcher.buildModel();

    Map<String, JavaType> types = new LinkedHashMap<>();
    launcher.getModel().getAllTypes().stream()
        .sorted(Comparator.comparing(CtType::getSimpleName))
        .forEach(type -> types.put(type.getSimpleName(), fromSpoonType(type)));
    return new JavaModel(types);
  }

  private static JavaType fromSpoonType(CtType<?> type) {
    TypeKind kind = spoonTypeKind(type);
    Set<String> enumConstants = new LinkedHashSet<>();
    Map<String, String> fields = new LinkedHashMap<>();
    Set<MethodSignature> methods = new LinkedHashSet<>();

    if (type instanceof CtEnum<?> enumType) {
      enumType.getEnumValues().stream()
          .sorted(Comparator.comparing(CtField::getSimpleName))
          .forEach(value -> enumConstants.add(value.getSimpleName()));
    } else if (!(type instanceof CtInterface<?>)) {
      type.getFields().stream()
          .sorted(Comparator.comparing(CtField::getSimpleName))
          .forEach(field -> fields.put(field.getSimpleName(), normalizeType(typeName(field.getType()))));
    }

    type.getMethods().stream()
        .filter(method -> isJavaExpressibleMethod(method.getSimpleName(), method.getParameters().size()))
        .map(GeneratedJavaOracle::fromSpoonMethod)
        .sorted()
        .forEach(methods::add);

    return new JavaType(
        kind,
        readSpoonSuperclass(type),
        true,
        readSpoonInterfaceNames(type),
        enumConstants,
        fields,
        methods);
  }

  private static TypeKind spoonTypeKind(CtType<?> type) {
    if (type instanceof CtInterface<?>) {
      return TypeKind.INTERFACE;
    }
    if (type instanceof CtEnum<?>) {
      return TypeKind.ENUM;
    }
    return TypeKind.CLASS;
  }

  private static MethodSignature fromSpoonMethod(CtMethod<?> method) {
    List<Parameter> parameters = new ArrayList<>();
    for (CtParameter<?> parameter : method.getParameters()) {
      parameters.add(new Parameter(parameter.getSimpleName(), normalizeType(typeName(parameter.getType()))));
    }
    return new MethodSignature(
        method.getSimpleName(),
        normalizeType(typeName(method.getType())),
        parameters);
  }

  private static Optional<String> readSpoonSuperclass(CtType<?> type) {
    if (type instanceof CtInterface<?> || type instanceof CtEnum<?> || type.getSuperclass() == null) {
      return Optional.empty();
    }
    return Optional.of(normalizeType(typeName(type.getSuperclass())));
  }

  private static Set<String> readSpoonInterfaceNames(CtType<?> type) {
    List<String> names =
        type.getSuperInterfaces().stream()
            .map(GeneratedJavaOracle::typeName)
            .map(GeneratedJavaOracle::normalizeType)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    names.sort(String::compareTo);
    return new LinkedHashSet<>(names);
  }

  private static String typeName(CtTypeReference<?> type) {
    if (type == null) {
      return "void";
    }
    String qualifiedName = type.getQualifiedName();
    return qualifiedName == null || qualifiedName.isBlank() ? type.getSimpleName() : qualifiedName;
  }

  private static TypeKind typeKind(ASTCDType type) {
    if (type instanceof ASTCDInterface) {
      return TypeKind.INTERFACE;
    }
    if (type instanceof ASTCDEnum) {
      return TypeKind.ENUM;
    }
    return TypeKind.CLASS;
  }

  private static Optional<String> readSuperclass(ASTCDType type) {
    return CDTypeRelations.firstSuperclassName(type).map(GeneratedJavaOracle::normalizeType);
  }

  private static boolean compareSuperclass(ASTCDType type) {
    return !(type instanceof ASTCDClass cdClass) || cdClass.getSuperclassList().size() <= 1;
  }

  private static List<String> readInterfaceNames(ASTCDType type) {
    List<String> names =
        CDTypeRelations.interfaceNames(type).stream()
            .map(GeneratedJavaOracle::normalizeType)
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    names.sort(String::compareTo);
    return names;
  }

  private static boolean isJavaExpressibleMethod(ASTCDType owner, ASTCDMethod method) {
    if (owner instanceof ASTCDInterface) {
      return true;
    }
    return isJavaExpressibleMethod(method.getName(), method.getCDParameterList().size());
  }

  private static boolean isJavaExpressibleMethod(String name, int parameterCount) {
    return !("notifyAll".equals(name) && parameterCount == 0)
        && !("notify".equals(name) && parameterCount == 0)
        && !("getClass".equals(name) && parameterCount == 0)
        && !("wait".equals(name) && parameterCount <= 2);
  }

  private static String printType(ASTMCType type) {
    return JavaLoader.print(type);
  }

  private static String normalizeType(String type) {
    return JavaSourceNames.normalizeType(type);
  }

  private enum TypeKind {
    CLASS,
    INTERFACE,
    ENUM
  }

  private record JavaModel(Map<String, JavaType> types) {}

  private record JavaType(
      TypeKind kind,
      Optional<String> superClass,
      boolean compareSuperclass,
      Set<String> interfaces,
      Set<String> enumConstants,
      Map<String, String> fields,
      Set<MethodSignature> methods) {}

  private record Parameter(String name, String type) implements Comparable<Parameter> {
    @Override
    public int compareTo(Parameter other) {
      int byName = name.compareTo(other.name);
      return byName != 0 ? byName : type.compareTo(other.type);
    }
  }

  private record MethodSignature(String name, String returnType, List<Parameter> parameters)
      implements Comparable<MethodSignature> {
    @Override
    public int compareTo(MethodSignature other) {
      int byName = name.compareTo(other.name);
      if (byName != 0) {
        return byName;
      }
      int byReturnType = returnType.compareTo(other.returnType);
      if (byReturnType != 0) {
        return byReturnType;
      }
      return parameters.toString().compareTo(other.parameters.toString());
    }
  }
}
