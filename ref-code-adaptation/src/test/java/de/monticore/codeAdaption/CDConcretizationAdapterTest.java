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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconcretization.ConcretizationCompleter;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.testutil.CDConcretizationTestCase;
import de.monticore.codeAdaption.testutil.CDConcretizationTestCases;
import de.monticore.codeAdaption.testutil.CDConcretizationFixtureWorkspace;
import de.monticore.codeAdaption.testutil.CompletedCDJavaProjector;
import de.monticore.codeAdaption.testutil.GeneratedJavaOracle;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CDConcretizationAdapterTest extends AdapterAbstractTest {

  private Set<CDConfParameter> confParameters;
  private Set<AdapterParam> adapterParams;

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

    Path projectionPath = writeCompletedCdProjection(materializedTestCase, confParameters);
    List<java.nio.file.Path> javaFiles = generatedJavaFiles(materializedTestCase, projectionPath);
    assertFalse(
        javaFiles.isEmpty(),
        () -> "No Java output generated for " + materializedTestCase.displayName());
    assertNoAdaptMetadata(materializedTestCase, javaFiles);
    assertCompiles(materializedTestCase, javaFiles);
    initMills();
    GeneratedJavaOracle.assertMatchesExpectedStructure(
        materializedTestCase, confParameters, javaFiles);
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
    Path projectionPath = projectionPath(testCase);
    deleteRecursively(testCase.outputPath());
    deleteRecursively(testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_formatted"));
    deleteRecursively(projectionPath);
    deleteRecursively(projectionPath.resolveSibling(projectionPath.getFileName() + "_formatted"));
  }

  private static Path writeCompletedCdProjection(
      CDConcretizationTestCase testCase, Set<CDConfParameter> confParameters) {
    initMills();
    Path projectionPath = projectionPath(testCase);
    ASTCDCompilationUnit completedConcreteCD = JavaLoader.parseCD(testCase.concCd().toString());
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(testCase.refCd().toString());
    try {
      new ConcretizationCompleter(confParameters)
          .completeCD(completedConcreteCD, refCD, new ArrayList<>(testCase.mappings()));
    } catch (Throwable t) {
      Log.warn(
          "CD concretization failed while building projection oracle for "
              + testCase.displayName()
              + ": "
              + t.getMessage());
    }
    CompletedCDJavaProjector.write(completedConcreteCD, projectionPath);
    return projectionPath;
  }

  private static Path projectionPath(CDConcretizationTestCase testCase) {
    return testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_projection");
  }

  private static void deleteRecursively(java.nio.file.Path path) {
    if (!path.startsWith(java.nio.file.Path.of(CDConcretizationTestCase.OUTPUT_ROOT))) {
      throw new IllegalArgumentException("Refusing to delete path outside test output root: " + path);
    }
    if (!java.nio.file.Files.exists(path)) {
      return;
    }
    try (Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(path)) {
      paths
          .sorted(Comparator.reverseOrder())
          .forEach(
              p -> {
                deletePathWithRetry(p);
              });
    } catch (IOException e) {
      throw new IllegalStateException("Failed to clean previous test output " + path, e);
    }
  }

  private static void deletePathWithRetry(java.nio.file.Path path) {
    IOException last = null;
    for (int attempt = 0; attempt < 5; attempt++) {
      try {
        java.nio.file.Files.deleteIfExists(path);
        return;
      } catch (IOException e) {
        last = e;
        try {
          Thread.sleep(100L * (attempt + 1));
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw new IllegalStateException("Interrupted while deleting test output " + path, interrupted);
        }
      }
    }
    throw new IllegalStateException("Failed to delete previous test output " + path, last);
  }

  private static List<java.nio.file.Path> generatedJavaFiles(
      CDConcretizationTestCase testCase, Path outputPath) {
    assertTrue(
        java.nio.file.Files.isDirectory(outputPath),
        () -> "Projection output directory was not created for " + testCase.displayName());
    try (Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(outputPath)) {
      return paths
          .filter(java.nio.file.Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".java"))
          .filter(CDConcretizationAdapterTest::isFinalOutputFile)
          .sorted()
          .toList();
    } catch (IOException e) {
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

  private static void assertCompiles(
      CDConcretizationTestCase testCase, List<java.nio.file.Path> javaFiles) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull(compiler, "A JDK compiler is required to verify generated Java output");

    java.nio.file.Path compileOutput;
    try {
      compileOutput = java.nio.file.Files.createTempDirectory("cdconcretization-compile-");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create compile output directory", e);
    }

    StringWriter compilerOutput = new StringWriter();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(null, null, null)) {
      Iterable<? extends javax.tools.JavaFileObject> compilationUnits =
          fileManager.getJavaFileObjectsFromPaths(javaFiles);
      List<String> options =
          List.of(
              "-classpath",
              System.getProperty("java.class.path"),
              "-d",
              compileOutput.toString());
      Boolean success =
          compiler
              .getTask(compilerOutput, fileManager, null, options, null, compilationUnits)
              .call();

      assertTrue(
          Boolean.TRUE.equals(success),
          () -> "Generated Java does not compile for "
              + testCase.displayName()
              + System.lineSeparator()
              + compilerOutput);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to compile generated output for " + testCase.displayName(), e);
    } finally {
      deleteTempDirectory(compileOutput);
    }
  }

  private static void deleteTempDirectory(java.nio.file.Path path) {
    if (path == null || !java.nio.file.Files.exists(path)) {
      return;
    }
    try (Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(path)) {
      paths
          .sorted(Comparator.reverseOrder())
          .forEach(
              p -> {
                try {
                  java.nio.file.Files.deleteIfExists(p);
                } catch (IOException ignored) {
                  // Temporary compile output is best-effort cleanup only.
                }
              });
    } catch (IOException ignored) {
      // Temporary compile output is best-effort cleanup only.
    }
  }
}
