package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.CodeAdapter;
import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CodeAdapterTestCase2a extends EvaluationAbstractTest {

  @BeforeEach
  public void setup() {
    referenceCD = new File(resourcesPath + "testcase_2_cd4code/Reference.cd");
    concreteCD = new File(resourcesPath + "testcase_2_cd4code/Concrete.cd");
    refCodePath = Path.of(resourcesPath + "testcase_2_cd4code/hwc/entity");
    conCodePath = Path.of(resourcesPath + "testcase_2_cd4code/concrete");
    output = temporaryDirectory.resolve("testcase_2_cd4code");
    initMills();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    confParameters =
        Set.of(
            INHERITANCE, STEREOTYPE_MAPPING, NAME_MAPPING, ADAPTED_NAME_MAPPING,
            SRC_TARGET_ASSOC_MAPPING);
  }

  @ParameterizedTest(name = "completion={0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("Evaluation Code Adapter Case Study 2 without concrete Java")
  public void adaptsAssociationRoleUsageWithoutRunningARegularGenerator(boolean useConcretization) {
    Set<String> mappings = Set.of("stud");
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    deleteRecursively(output);

    assertDoesNotThrow(
        () ->
            adapter.adaptWithoutConcreteCode(
                referenceCD, concreteCD, mappings, refCodePath, output, useConcretization, true));
    assertModelConforms(output, useConcretization);

    String adaptedStudent = readFileContent(output, "Student.java");
    assertTrue(adaptedStudent.contains("package Concrete;"));
    assertTrue(adaptedStudent.contains("class Student extends StudentTOP"));
    assertTrue(adaptedStudent.contains("printHiwiRoles()"));
    assertTrue(
        adaptedStudent.contains(
            "for (HiwiRole hiwiRole : this." + roleName(useConcretization) + ")"));
    assertTrue(adaptedStudent.contains("roleNames = hiwiRole.name"));
    assertFalse(adaptedStudent.contains("Role role"));
    assertFalse(adaptedStudent.contains("printRoles()"));

    assertFalse(generatedFileNames(output).contains("StudentTOP.java"));
    assertFalse(generatedFileNames(output).contains("HiwiRole.java"));
    assertNoAdapterMetadata(output);
  }

  @ParameterizedTest(name = "completion={0}")
  @ValueSource(booleans = {false, true})
  void suppliedJavaEmptyConcreteDirectoryPreservesResourcesWithoutGeneration(boolean useConcretization)
      throws IOException {
    Path emptyConcrete = Files.createDirectories(temporaryDirectory.resolve("concrete"));
    Files.writeString(emptyConcrete.resolve("settings.txt"), "preserve-me");
    Path generatedOutput = temporaryDirectory.resolve("generated-with-resource");

    new CodeAdapter(adapterParams, confParameters)
        .adapt(
            referenceCD,
            concreteCD,
            Set.of("stud"),
            refCodePath,
            emptyConcrete,
            generatedOutput,
            useConcretization,
            true);
    assertModelConforms(generatedOutput, useConcretization);

    assertTrue(Files.isRegularFile(generatedOutput.resolve("Concrete/Student.java")));
    assertFalse(Files.isRegularFile(generatedOutput.resolve("Concrete/StudentTOP.java")));
    assertTrue(Files.isRegularFile(generatedOutput.resolve("settings.txt")));
  }

  @ParameterizedTest(name = "completion={0}")
  @ValueSource(booleans = {false, true})
  void emptyReferenceDirectoryDoesNotGenerateConcreteModel(boolean useConcretization)
      throws IOException {
    Path emptyReference = Files.createDirectories(temporaryDirectory.resolve("empty-reference"));
    Path generatedOutput = temporaryDirectory.resolve("model-only-output");

    new CodeAdapter(adapterParams, confParameters)
        .adaptWithoutConcreteCode(
            referenceCD, concreteCD, Set.of("stud"), emptyReference, generatedOutput,
            useConcretization, true);
    assertModelConforms(generatedOutput, useConcretization);

    assertTrue(generatedJavaFiles(generatedOutput).isEmpty());
  }

  @ParameterizedTest(name = "completion={0}")
  @ValueSource(booleans = {false, true})
  void topApiAddsInheritanceAndEmitsAdaptedImplementationAsTop(boolean useConcretization)
      throws Exception {
    Path topOutput = assertTopWithCd2Pojo(useConcretization, roleName(useConcretization));
    assertModelConforms(topOutput, useConcretization);
  }

  @Test
  void manualModeUsesHiwiRolesWhenTheConcreteModelDeclaresHiwiRoles() throws Exception {
    // Keep the checked-in model unchanged; manual adaptation and CDGen consume the same
    // explicitly named concrete model, just as they would in a user project.
    Path namedModel = temporaryDirectory.resolve("Concrete.cd");
    Files.writeString(
        namedModel, Files.readString(concreteCD.toPath()).replace("(roles)", "(hiwiRoles)"));
    concreteCD = namedModel.toFile();
    assertTrue(
        new CDConformanceChecker(confParameters)
            .checkConformance(
                JavaLoader.parseCD(concreteCD.getPath()),
                JavaLoader.parseCD(referenceCD.getPath()), "stud"));
    assertTopWithCd2Pojo(false, "hiwiRoles");
  }

  private Path assertTopWithCd2Pojo(boolean useConcretization, String expectedRole)
      throws Exception {
    Path concrete = Files.createDirectories(temporaryDirectory.resolve("top-concrete"));
    Files.writeString(
        concrete.resolve("Student.java"), "package Concrete; public class Student {}");
    Path topOutput = temporaryDirectory.resolve("top-output");

    new CodeAdapter(adapterParams, confParameters)
        .adaptWithTopSeparation(
            referenceCD, concreteCD, Set.of("stud"), refCodePath, concrete, topOutput,
            useConcretization, true);

    String handwrittenStudent = readFileContent(topOutput, "Student.java");
    String adaptedTop = readFileContent(topOutput, "StudentTOP.java");
    assertTrue(handwrittenStudent.contains("class Student extends StudentTOP"));
    assertTrue(adaptedTop.contains("class StudentTOP extends StudentTOPTOP"));
    assertTrue(adaptedTop.contains("printHiwiRoles()"));
    assertTrue(
        adaptedTop.contains("for (HiwiRole hiwiRole : this." + expectedRole + ")"));
    if (expectedRole.equals("hiwiRoles")) {
      assertFalse(adaptedTop.contains("this.roles"));
    }
    Path generationModel =
        useConcretization ? topOutput.resolve(concreteCD.getName()) : concreteCD.toPath();
    Path generated = temporaryDirectory.resolve("cd2pojo");
    Cd2PojoTestSupport.generateWithAdaptedTopLayer(generationModel, topOutput, generated);
    assertTrue(generatedFileNames(generated).contains("StudentTOPTOP.java"));
    assertTrue(generatedFileNames(generated).contains("HiwiRole.java"));
    assertFalse(generatedFileNames(generated).contains("StudentTOP.java"));
    assertNoAdapterMetadata(topOutput);
    Cd2PojoTestSupport.compileAndExercise(
        topOutput, generated, temporaryDirectory.resolve("compiled"), expectedRole);
    return topOutput;
  }

  private void assertModelConforms(Path generatedOutput, boolean useConcretization) {
    Path checkedConcrete =
        useConcretization ? generatedOutput.resolve(concreteCD.getName()) : concreteCD.toPath();
    assertTrue(Files.isRegularFile(checkedConcrete));
    var checkedCD = JavaLoader.parseCD(checkedConcrete.toString());
    assertEquals(
        roleName(useConcretization),
        checkedCD.getCDDefinition().getCDAssociationsList().get(0).getRight().getCDRole().getName());
    assertTrue(
        new CDConformanceChecker(confParameters)
            .checkConformance(checkedCD, JavaLoader.parseCD(referenceCD.getPath()), "stud"));
    assertEquals(
        "roles",
        JavaLoader.parseCD(concreteCD.getPath())
            .getCDDefinition()
            .getCDAssociationsList()
            .get(0)
            .getRight()
            .getCDRole()
            .getName());
  }

  private static String roleName(boolean useConcretization) {
    return useConcretization ? "hiwiRoles" : "roles";
  }
}
