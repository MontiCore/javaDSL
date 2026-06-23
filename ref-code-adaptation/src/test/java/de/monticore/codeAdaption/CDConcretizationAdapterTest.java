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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
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

  static Stream<CDConcretizationTestCase> expectedFailureCases() {
    return CDConcretizationTestCases.expectedFailureCases().stream();
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
    assertNoWildcardJdkImports(materializedTestCase, javaFiles);
    assertNoAdaptMetadata(materializedTestCase, javaFiles);
    assertGeneratedJavaCompiles(javaFiles);
    initMills();
    GeneratedJavaOracle.assertMatchesExpectedStructure(
        materializedTestCase, confParameters, javaFiles);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("expectedFailureCases")
  void rejectsUnderspecifiedMethodTypesWithoutIncarnation(CDConcretizationTestCase testCase) {
    Log.enableFailQuick(false);
    CDConcretizationTestCase materializedTestCase =
        CDConcretizationFixtureWorkspace.materialize(testCase);
    confParameters = defaultConformanceParams(materializedTestCase.strictParameterOrder());

    ASTCDCompilationUnit completedConcreteCD = JavaLoader.parseCD(materializedTestCase.concCd().toString());
    ASTCDCompilationUnit refCD = JavaLoader.parseCD(materializedTestCase.refCd().toString());
    try {
      new ConcretizationCompleter(confParameters)
          .completeCD(completedConcreteCD, refCD, new ArrayList<>(materializedTestCase.mappings()));
    } catch (Exception ignored) {
    }

    assertTrue(
        Log.getFindings().stream()
            .anyMatch(
                finding ->
                    finding
                        .getMsg()
                        .contains(
                            "Underspecified placeholder type not allowed in method without incarnations")),
        () -> "Expected underspecified method-type diagnostic for " + materializedTestCase.displayName());
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
    deleteOutputPath(testCase.outputPath());
    deleteOutputPath(testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_formatted"));
    deleteOutputPath(projectionPath);
    deleteOutputPath(projectionPath.resolveSibling(projectionPath.getFileName() + "_formatted"));
  }

  private static Path writeCompletedCdProjection(
      CDConcretizationTestCase testCase, Set<CDConfParameter> confParameters) {
    initMills();
    Path projectionPath = projectionPath(testCase);
    ASTCDCompilationUnit completedConcreteCD =
        expectedOutCd(testCase)
            .map(path -> JavaLoader.parseCD(path.toString()))
            .orElseGet(() -> completeProjectionCd(testCase, confParameters));
    CompletedCDJavaProjector.write(completedConcreteCD, projectionPath);
    return projectionPath;
  }

  private static java.util.Optional<Path> expectedOutCd(CDConcretizationTestCase testCase) {
    String concName = testCase.concCd().getFileName().toString();
    Path out = testCase.concCd().resolveSibling(concName.replace("Conc.cd", "Out.cd"));
    return java.nio.file.Files.exists(out) ? java.util.Optional.of(out) : java.util.Optional.empty();
  }

  private static ASTCDCompilationUnit completeProjectionCd(
      CDConcretizationTestCase testCase, Set<CDConfParameter> confParameters) {
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
    return completedConcreteCD;
  }

  private static Path projectionPath(CDConcretizationTestCase testCase) {
    return testCase.outputPath().resolveSibling(testCase.outputPath().getFileName() + "_projection");
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
        () -> "Generated projection contains wildcard JDK imports for "
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

}
