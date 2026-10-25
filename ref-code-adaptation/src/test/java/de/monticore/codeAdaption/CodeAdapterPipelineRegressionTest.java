package de.monticore.codeAdaption;

import static de.monticore.cdconformance.CDConfParameter.ADAPTED_NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS;
import static de.monticore.cdconformance.CDConfParameter.ALLOW_CARD_RESTRICTION;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.METHOD_OVERLOADING;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Regression coverage for merge and output behavior across the complete adaptation pipeline. */
class CodeAdapterPipelineRegressionTest extends AdapterAbstractTest {

  private static final Set<CDConfParameter> CONCRETIZATION_PARAMETERS =
      Set.of(
          STEREOTYPE_MAPPING,
          NAME_MAPPING,
          SRC_TARGET_ASSOC_MAPPING,
          INHERITANCE,
          ALLOW_CARD_RESTRICTION,
          METHOD_OVERLOADING,
          ADAPTED_NAME_MAPPING,
          ALLOW_ADDITIONAL_PARAMETERS);

  @TempDir Path temporaryDirectory;

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void divergentMethodBodiesFromAdaptationPassesMustNotBeSilentlyDiscarded() throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit("first/Service.java", "package p; class Service { int value() { return 1; } }");
    ASTOrdinaryCompilationUnit second =
        javaUnit("second/Service.java", "package p; class Service { int value() { return 2; } }");

    assertThrows(IllegalStateException.class, () -> AdapterUtils.mergeAsts(first, second));
  }

  @Test
  void divergentFieldDefinitionsFromAdaptationPassesMustNotBeSilentlyDiscarded()
      throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit(
            "first/Service.java",
            "package p; class Service { static final int value = 1; }");
    ASTOrdinaryCompilationUnit second =
        javaUnit("second/Service.java", "package p; class Service { int value = 2; }");

    assertThrows(IllegalStateException.class, () -> AdapterUtils.mergeAsts(first, second));
  }

  @Test
  void divergentSuperclassesFromAdaptationPassesMustNotBeSilentlyDiscarded()
      throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit("first/Service.java", "package p; class Service extends FirstBase {}");
    ASTOrdinaryCompilationUnit second =
        javaUnit("second/Service.java", "package p; class Service extends SecondBase {}");

    assertThrows(IllegalStateException.class, () -> AdapterUtils.mergeAsts(first, second));
  }

  @Test
  void divergentNestedTypesFromAdaptationPassesMustNotBeSilentlyDiscarded()
      throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit(
            "first/Service.java",
            "package p; class Service { static class State { int value() { return 1; } } }");
    ASTOrdinaryCompilationUnit second =
        javaUnit(
            "second/Service.java",
            "package p; class Service { static class State { int value() { return 2; } } }");

    assertThrows(IllegalStateException.class, () -> AdapterUtils.mergeAsts(first, second));
  }

  @Test
  void qualifiedSameSimpleParameterTypesRemainDistinctOverloads() throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit(
            "qualified-first/Service.java",
            "package p; class Service { void run(alpha.Role role) {} }");
    ASTOrdinaryCompilationUnit second =
        javaUnit(
            "qualified-second/Service.java",
            "package p; class Service { void run(beta.Role role) {} }");

    String merged = JavaLoader.print(AdapterUtils.mergeAsts(first, second));

    assertTrue(merged.contains("alpha.Role"), merged);
    assertTrue(merged.contains("beta.Role"), merged);
  }

  @Test
  void genericArgumentsDoNotCreateFalseJavaOverloads() throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit(
            "generic-first/Service.java",
            "package p; class Service { void run(java.util.List<String> values) {} }");
    ASTOrdinaryCompilationUnit second =
        javaUnit(
            "generic-second/Service.java",
            "package p; class Service { void run(java.util.List<Integer> values) {} }");

    assertThrows(IllegalStateException.class, () -> AdapterUtils.mergeAsts(first, second));
  }

  @Test
  void importedAndQualifiedSuperclassBindingsAreTheSameIdentity() throws IOException {
    ASTOrdinaryCompilationUnit imported =
        javaUnit(
            "imported-super/Service.java",
            "package p; import alpha.Base; class Service extends Base {}");
    ASTOrdinaryCompilationUnit qualified =
        javaUnit(
            "qualified-super/Service.java",
            "package p; class Service extends alpha.Base {}");

    assertDoesNotThrow(() -> AdapterUtils.mergeAsts(imported, qualified));
  }

  @Test
  void formattingAndCommentsDoNotCreateMergeConflicts() throws IOException {
    ASTOrdinaryCompilationUnit first =
        javaUnit(
            "formatted-first/Service.java",
            "package p; class Service { int value() { return 1; } }");
    ASTOrdinaryCompilationUnit second =
        javaUnit(
            "formatted-second/Service.java",
            "package p; class Service { /* same behavior */ int value(){return 1;} }");

    assertDoesNotThrow(() -> AdapterUtils.mergeAsts(first, second));
  }

  @Test
  void concreteCompilationUnitWithSeveralTopLevelTypesMustProduceCompilableOutput()
      throws Exception {
    Path concrete = temporaryDirectory.resolve("concrete");
    Path staging = temporaryDirectory.resolve("staging");
    Files.createDirectories(concrete);
    Files.createDirectories(staging.resolve("p"));
    Files.writeString(
        concrete.resolve("Service.java"),
        "package p; class Helper { int handwritten; } public class Service {}");
    Files.writeString(
        staging.resolve("p/Helper.java"),
        "package p; class Helper { int adapted; }");

    new OutputCodeService().copyConcreteFiles(concrete, staging);

    assertGeneratedJavaCompiles(generatedJavaFilesRecursively(staging));
  }

  @Test
  void helperReachableOnlyFromRetainedIgnoredRootMustBeIncluded() throws IOException {
    Path fixture = temporaryDirectory.resolve("ignored-root-helper");
    Path referenceCode = fixture.resolve("reference-code");
    Path concreteCode = fixture.resolve("concrete-code");
    Path output = fixture.resolve("output");
    Files.createDirectories(referenceCode.resolve("demo"));
    Files.createDirectories(concreteCode.resolve("demo"));
    Path referenceCD = fixture.resolve("Reference.cd");
    Path concreteCD = fixture.resolve("Concrete.cd");
    Files.writeString(referenceCD, "classdiagram Reference { class Service; }");
    Files.writeString(
        concreteCD,
        "classdiagram Concrete { <<ref=\"Service\">> class ConcreteService; }");
    Files.writeString(
        referenceCode.resolve("demo/IgnoredFacade.java"),
        """
        package demo;
        import de.monticore.codeAdaption.utils.Adapt;
        @Adapt(ignore = true)
        public class IgnoredFacade {
          private final Helper helper = new Helper();
          public Object helper() { return helper; }
        }
        """);
    Files.writeString(
        referenceCode.resolve("demo/Helper.java"),
        "package demo; final class Helper {}");
    Files.writeString(
        concreteCode.resolve("demo/ConcreteService.java"),
        "package demo; public class ConcreteService {}");

    new CodeAdapter(Set.of(NAME_MATCHING, ANNOTATION_MATCHING), CONCRETIZATION_PARAMETERS)
        .adapt(
            referenceCD.toFile(),
            concreteCD.toFile(),
            Set.of("ref"),
            referenceCode,
            concreteCode,
            output,
            true,
            true);

    assertTrue(
        Files.isRegularFile(output.resolve("demo/Helper.java")),
        "The dependency closure must start at explicitly retained ignored roots");
    assertGeneratedJavaCompiles(generatedJavaFilesRecursively(output));
  }

  private ASTOrdinaryCompilationUnit javaUnit(String relativePath, String source)
      throws IOException {
    Path file = temporaryDirectory.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, source);
    return JavaLoader.loadJava(file.toFile());
  }

}
