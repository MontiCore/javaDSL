package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.ADAPTED_NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_CARD_RESTRICTION;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.METHOD_OVERLOADING;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STRICT_PARAMETER_ORDER;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.testutil.CDConcretizationTestCase;
import de.monticore.codeAdaption.testutil.CDConcretizationTestCases;
import de.monticore.codeAdaption.testutil.CDConcretizationFixtureWorkspace;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.LogStub;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CDConcretizationAdapterTest extends AdapterAbstractTest {

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

  @TempDir Path temporaryDirectory;

  @BeforeEach
  public void setup() {
    LogStub.init();
    initMills();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
  }

  static Stream<CDConcretizationTestCase> testCases() {
    return CDConcretizationTestCases.enabledCases().stream();
  }

  static Stream<CDConcretizationTestCase> expectedFailureCases() {
    return CDConcretizationTestCases.expectedFailureCases().stream();
  }

  static Stream<CDConcretizationTestCase> unsupportedCases() {
    return CDConcretizationTestCases.unsupportedCases().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void adaptsWithConcretizationCompleter(CDConcretizationTestCase testCase) {
    CDConcretizationTestCase materializedTestCase =
        CDConcretizationFixtureWorkspace.materialize(testCase);
    confParameters = defaultConformanceParams(materializedTestCase.strictParameterOrder());
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    cleanPreviousOutput(materializedTestCase);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                materializedTestCase.refCd().toFile(),
                materializedTestCase.concCd().toFile(),
                materializedTestCase.mappings(),
                materializedTestCase.adapterPath(),
                materializedTestCase.concretePath(),
                materializedTestCase.outputPath(),
                true,
                true),
        () -> "Adaptation failed for " + materializedTestCase.displayName());

    List<java.nio.file.Path> javaFiles =
        generatedJavaFiles(materializedTestCase, materializedTestCase.outputPath());
    assertFalse(
        javaFiles.isEmpty(),
        () -> "No Java output generated for " + materializedTestCase.displayName());
    assertNoWildcardJdkImports(materializedTestCase, javaFiles);
    assertNoAdaptMetadata(materializedTestCase, javaFiles);

    assertGeneratedJavaCompiles(compilationSources(javaFiles, materializedTestCase));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("expectedFailureCases")
  void rejectsInvalidConcretizationWithoutReplacingOutput(CDConcretizationTestCase testCase)
      throws IOException {
    assertRejectedWithoutReplacingOutput(testCase);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("unsupportedCases")
  void rejectsDocumentedUpstreamLimitationWithoutReplacingOutput(
      CDConcretizationTestCase testCase) throws IOException {
    assertFalse(CDConcretizationTestCases.unsupportedReason(testCase).isBlank());
    assertRejectedWithoutReplacingOutput(testCase);
  }

  @Test
  void compilationOracleRejectsUnadaptedReferenceType() throws IOException {
    CDConcretizationTestCase testCase = oracleTestCase("stale-reference");
    write(testCase.refCd(), "classdiagram Reference { class ReferenceType; }");
    write(testCase.concCd(), "classdiagram Concrete { class ConcreteType; }");
    Path adapted =
        write(
            testCase.outputPath().resolve("adapted/UsesReference.java"),
            "package adapted; public class UsesReference { ReferenceType value; }");

    List<Path> sources = compilationSources(List.of(adapted), testCase);

    assertFalse(
        sources.stream()
            .anyMatch(path -> path.getFileName().toString().equals("ReferenceType.java")));
    assertThrows(AssertionError.class, () -> assertGeneratedJavaCompiles(sources));
  }

  @Test
  void compilationOracleRejectsConcreteTypeInWrongPackage() throws IOException {
    CDConcretizationTestCase testCase = oracleTestCase("wrong-package");
    write(testCase.refCd(), "classdiagram Reference { class ReferenceType; }");
    write(testCase.concCd(), "classdiagram Concrete { class ConcreteType; }");
    write(
        testCase.concretePath().resolve("Anchor.java"),
        "package expected;\npublic class Anchor {}");
    Path adapted =
        write(
            testCase.outputPath().resolve("wrong/UsesConcrete.java"),
            "package wrong; public class UsesConcrete { ConcreteType value; }");

    List<Path> sources = compilationSources(List.of(adapted), testCase);

    assertTrue(
        sources.stream()
            .anyMatch(
                path ->
                    path.toString()
                        .replace('\\', '/')
                        .endsWith("/expected/ConcreteType.java")));
    assertThrows(AssertionError.class, () -> assertGeneratedJavaCompiles(sources));
  }

  @Test
  void compilationOracleAllowsOriginalConcreteTypeFromExternalGeneration()
      throws IOException {
    CDConcretizationTestCase testCase = oracleTestCase("external-concrete-type");
    write(testCase.refCd(), "classdiagram Reference { class ReferenceType; }");
    write(testCase.concCd(), "classdiagram Concrete { class ConcreteType; }");
    write(
        testCase.concretePath().resolve("Anchor.java"),
        "package expected;\npublic class Anchor {}");
    Path adapted =
        write(
            testCase.outputPath().resolve("expected/UsesConcrete.java"),
            "package expected; public class UsesConcrete { ConcreteType value; }");

    List<Path> sources = compilationSources(List.of(adapted), testCase);

    assertTrue(
        sources.stream()
            .anyMatch(
                path ->
                    path.toString()
                        .replace('\\', '/')
                        .endsWith("/expected/ConcreteType.java")));
    assertDoesNotThrow(() -> assertGeneratedJavaCompiles(sources));
  }

  private void assertRejectedWithoutReplacingOutput(CDConcretizationTestCase testCase)
      throws IOException {
    CDConcretizationTestCase materializedTestCase =
        CDConcretizationFixtureWorkspace.materialize(testCase);
    confParameters = defaultConformanceParams(materializedTestCase.strictParameterOrder());
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    cleanPreviousOutput(materializedTestCase);
    Files.createDirectories(materializedTestCase.outputPath());
    Path marker = materializedTestCase.outputPath().resolve("existing-output.txt");
    Files.writeString(marker, "preserve", StandardCharsets.UTF_8);

    assertThrows(
        IllegalStateException.class,
        () ->
            adapter.adapt(
                materializedTestCase.refCd().toFile(),
                materializedTestCase.concCd().toFile(),
                materializedTestCase.mappings(),
                materializedTestCase.adapterPath(),
                materializedTestCase.concretePath(),
                materializedTestCase.outputPath(),
                true,
                true));
    assertTrue(Files.exists(marker), "A failed run must preserve the previous output");
    assertEquals("preserve", Files.readString(marker, StandardCharsets.UTF_8));
  }

  private static Set<CDConfParameter> defaultConformanceParams(boolean strictParameterOrder) {
    Set<CDConfParameter> params = new LinkedHashSet<>();
    params.add(STEREOTYPE_MAPPING);
    params.add(NAME_MAPPING);
    params.add(SRC_TARGET_ASSOC_MAPPING);
    params.add(INHERITANCE);
    params.add(ALLOW_CARD_RESTRICTION);
    params.add(METHOD_OVERLOADING);
    params.add(ADAPTED_NAME_MAPPING);
    params.add(ALLOW_ADDITIONAL_PARAMETERS);
    if (strictParameterOrder) {
      params.add(STRICT_PARAMETER_ORDER);
    }
    return params;
  }

  private static void cleanPreviousOutput(CDConcretizationTestCase testCase) {
    deleteOutputPath(testCase.outputPath());
    deleteOutputPath(testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_formatted"));
    deleteOutputPath(testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_generated"));
    deleteOutputPath(
        testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_compile_stubs"));
  }

  private static List<Path> compilationSources(
      List<Path> adaptedJava, CDConcretizationTestCase testCase) {
    Map<String, Path> sourcesByType = new LinkedHashMap<>();
    adaptedJava.forEach(path -> sourcesByType.put(sourceTypeName(path), path));
    Path concretePath = testCase.concretePath();
    if (Files.isDirectory(concretePath)) {
      generatedJavaFilesRecursively(concretePath).forEach(
          path -> sourcesByType.putIfAbsent(sourceTypeName(path), path));
    }
    addConcreteModelStubs(testCase, sourcesByType);
    return new ArrayList<>(sourcesByType.values());
  }

  private static void addConcreteModelStubs(
      CDConcretizationTestCase testCase, Map<String, Path> sourcesByType) {
    Map<String, String> declarations = new LinkedHashMap<>();
    initMills();
    addModelDeclarations(JavaLoader.parseCD(testCase.concCd().toString()), declarations);

    Set<String> concretePackages =
        Files.isDirectory(testCase.concretePath())
            ? generatedJavaFilesRecursively(testCase.concretePath()).stream()
                .map(CDConcretizationAdapterTest::packageName)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new))
            : Set.of();
    if (concretePackages.size() != 1) {
      return;
    }
    String packageName = concretePackages.iterator().next();
    Path stubRoot =
        testCase
            .outputPath()
            .resolveSibling(testCase.outputPath().getFileName() + "_compile_stubs");
    Map<String, Path> generatedStubs = new LinkedHashMap<>();
    for (Map.Entry<String, String> declaration : declarations.entrySet()) {
      String qualifiedTypeName = qualify(packageName, declaration.getKey());
      if (sourcesByType.containsKey(qualifiedTypeName)) {
        continue;
      }
      Path packageDirectory =
          packageName.isBlank()
              ? stubRoot
              : stubRoot.resolve(packageName.replace('.', java.io.File.separatorChar));
      Path source = packageDirectory.resolve(declaration.getKey() + ".java");
      Path conflict = generatedStubs.putIfAbsent(qualifiedTypeName, source);
      if (conflict != null && !conflict.equals(source)) {
        throw new IllegalStateException(
            "Conflicting compile stubs for "
                + qualifiedTypeName
                + ": "
                + conflict
                + " and "
                + source);
      }
      String body =
          declaration.getValue().equals("enum")
              ? "public enum " + declaration.getKey() + " {;}"
              : "public " + declaration.getValue() + " " + declaration.getKey() + " {}";
      String content =
          (packageName.isBlank() ? "" : "package " + packageName + ";\n") + body + "\n";
      try {
        Files.createDirectories(packageDirectory);
        Files.writeString(source, content, StandardCharsets.UTF_8);
      } catch (IOException exception) {
        throw new IllegalStateException("Failed to write compile stub " + source, exception);
      }
      sourcesByType.put(qualifiedTypeName, source);
    }
  }

  private static void addModelDeclarations(
      ASTCDCompilationUnit cd, Map<String, String> declarations) {
    cd
        .getCDDefinition()
        .getCDClassesList()
        .forEach(type -> addModelDeclaration(declarations, type.getName(), "class"));
    cd
        .getCDDefinition()
        .getCDInterfacesList()
        .forEach(type -> addModelDeclaration(declarations, type.getName(), "interface"));
    cd
        .getCDDefinition()
        .getCDEnumsList()
        .forEach(type -> addModelDeclaration(declarations, type.getName(), "enum"));
  }

  private static void addModelDeclaration(
      Map<String, String> declarations, String typeName, String kind) {
    String conflict = declarations.putIfAbsent(typeName, kind);
    if (conflict != null) {
      throw new IllegalStateException(
          "Conflicting concrete model declarations for "
              + typeName
              + ": "
              + conflict
              + " and "
              + kind);
    }
  }

  private static String sourceTypeName(Path source) {
    String fileName = source.getFileName().toString();
    String simpleName = fileName.substring(0, fileName.length() - ".java".length());
    return qualify(packageName(source), simpleName);
  }

  private static String qualify(String packageName, String simpleName) {
    return packageName.isBlank() ? simpleName : packageName + "." + simpleName;
  }

  private static String packageName(Path source) {
    try {
      String packageName = "";
      for (String line : Files.readAllLines(source, StandardCharsets.UTF_8)) {
        String trimmed = line.strip();
        if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
          packageName = trimmed.substring("package ".length(), trimmed.length() - 1).strip();
          break;
        }
      }
      return packageName;
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to inspect Java source " + source, exception);
    }
  }

  private static void deleteOutputPath(java.nio.file.Path path) {
    if (!path.startsWith(java.nio.file.Path.of(CDConcretizationTestCase.OUTPUT_ROOT))) {
      throw new IllegalArgumentException("Refusing to delete path outside test output root: " + path);
    }
    deleteRecursively(path);
  }

  private static List<java.nio.file.Path> generatedJavaFiles(
      CDConcretizationTestCase testCase, Path outputPath) {
    try {
      return generatedJavaFilesRecursively(outputPath).stream()
          .filter(CDConcretizationAdapterTest::isFinalOutputFile)
          .sorted()
          .toList();
    } catch (RuntimeException e) {
      throw new IllegalStateException("Failed to inspect generated output for " + testCase.displayName(), e);
    }
  }

  private static boolean isFinalOutputFile(java.nio.file.Path path) {
    for (java.nio.file.Path part : path) {
      String name = part.toString();
      if (name.startsWith("_temp_") || name.endsWith("_formatted")) {
        return false;
      }
    }
    return true;
  }

  private static void assertNoAdaptMetadata(
      CDConcretizationTestCase testCase, List<java.nio.file.Path> javaFiles) {
    List<java.nio.file.Path> filesWithMetadata =
        javaFiles.stream()
            .filter(CDConcretizationAdapterTest::containsAdaptMetadata)
            .toList();

    assertTrue(
        filesWithMetadata.isEmpty(),
        () -> "Generated output still contains @Adapt metadata for "
            + testCase.displayName()
            + ": "
            + filesWithMetadata);
  }

  private static boolean containsAdaptMetadata(java.nio.file.Path path) {
    try {
      String source = java.nio.file.Files.readString(path);
      return source.contains("@Adapt")
          || source.contains("import de.monticore.codeAdaption.utils.Adapt");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read generated Java file " + path, e);
    }
  }

  private static void assertNoWildcardJdkImports(
      CDConcretizationTestCase testCase, List<java.nio.file.Path> javaFiles) {
    List<java.nio.file.Path> filesWithWildcards =
        javaFiles.stream()
            .filter(CDConcretizationAdapterTest::containsWildcardJdkImport)
            .toList();

    assertTrue(
        filesWithWildcards.isEmpty(),
        () -> "Generated output contains wildcard JDK imports for "
            + testCase.displayName()
            + ": "
            + filesWithWildcards);
  }

  private static boolean containsWildcardJdkImport(java.nio.file.Path path) {
    try {
      String source = java.nio.file.Files.readString(path);
      return source.contains("import java.util.*;") || source.contains("import java.time.*;");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read generated Java file " + path, e);
    }
  }

  private CDConcretizationTestCase oracleTestCase(String name) throws IOException {
    Path root = temporaryDirectory.resolve(name);
    Files.createDirectories(root.resolve("concrete"));
    Files.createDirectories(root.resolve("output"));
    return new CDConcretizationTestCase(
        name,
        name,
        root.resolve("Reference.cd"),
        root.resolve("Concrete.cd"),
        root.resolve("adapter"),
        root.resolve("concrete"),
        root.resolve("output"),
        false,
        true);
  }

  private static Path write(Path path, String content) throws IOException {
    Files.createDirectories(path.getParent());
    Files.writeString(path, content + System.lineSeparator(), StandardCharsets.UTF_8);
    return path;
  }

}
