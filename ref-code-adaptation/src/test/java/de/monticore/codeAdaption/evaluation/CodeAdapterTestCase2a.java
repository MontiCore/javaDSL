package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.codeAdaption.CodeAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CodeAdapterTestCase2a extends EvaluationAbstractTest {

  @BeforeEach
  public void setup() {
    referenceCD = new File(resourcesPath + "testcase_2_cd4code/Reference.cd");
    concreteCD = new File(resourcesPath + "testcase_2_cd4code/Concrete.cd");
    refCodePath = Path.of(resourcesPath + "testcase_2_cd4code/hwc/entity");
    conCodePath = Path.of(resourcesPath + "testcase_2_cd4code/concrete");
    output = temporaryDirectory.resolve("testcase_2_cd4code");
    CD4CodeMill.init();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    confParameters = Set.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 2 without concrete Java")
  public void adaptsAssociationRoleUsageWithoutRunningARegularGenerator() {
    Set<String> mappings = Set.of("stud");
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    deleteRecursively(output);

    assertDoesNotThrow(
        () -> adapter.adaptWithoutConcreteCode(referenceCD, concreteCD, mappings, refCodePath, output));

    String adaptedStudent = readFileContent(output, "Student.java");
    assertTrue(adaptedStudent.contains("package Concrete;"));
    assertTrue(adaptedStudent.contains("class Student extends StudentTOP"));
    assertTrue(adaptedStudent.contains("printHiwiRoles()"));
    assertTrue(adaptedStudent.contains("for (HiwiRole hiwiRole : this.roles)"));
    assertTrue(adaptedStudent.contains("roleNames = hiwiRole.name"));
    assertFalse(adaptedStudent.contains("Role role"));
    assertFalse(adaptedStudent.contains("printRoles()"));

    assertFalse(generatedFileNames(output).contains("StudentTOP.java"));
    assertFalse(generatedFileNames(output).contains("HiwiRole.java"));
    assertNoAdapterMetadata(output);
  }

  @Test
  void suppliedJavaEmptyConcreteDirectoryPreservesResourcesWithoutGeneration()
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
            generatedOutput);

    assertTrue(Files.isRegularFile(generatedOutput.resolve("Concrete/Student.java")));
    assertFalse(Files.isRegularFile(generatedOutput.resolve("Concrete/StudentTOP.java")));
    assertTrue(Files.isRegularFile(generatedOutput.resolve("settings.txt")));
  }

  @Test
  void emptyReferenceDirectoryDoesNotGenerateConcreteModel() throws IOException {
    Path emptyReference = Files.createDirectories(temporaryDirectory.resolve("empty-reference"));
    Path generatedOutput = temporaryDirectory.resolve("model-only-output");

    new CodeAdapter(adapterParams, confParameters)
        .adaptWithoutConcreteCode(
            referenceCD, concreteCD, Set.of("stud"), emptyReference, generatedOutput);

    assertTrue(generatedJavaFiles(generatedOutput).isEmpty());
  }

  @Test
  void topApiAddsInheritanceAndEmitsAdaptedImplementationAsTop() throws IOException {
    Path concrete = Files.createDirectories(temporaryDirectory.resolve("top-concrete"));
    Files.writeString(
        concrete.resolve("Student.java"), "package Concrete; public class Student {}");
    Files.writeString(
        concrete.resolve("StudentTOPTOP.java"),
        "package Concrete; import java.util.LinkedHashSet; import java.util.Set; "
            + "public abstract class StudentTOPTOP { "
            + "protected Set<HiwiRole> roles = new LinkedHashSet<>(); }");
    Files.writeString(
        concrete.resolve("HiwiRole.java"),
        "package Concrete; public class HiwiRole { String name; }");
    Path topOutput = temporaryDirectory.resolve("top-output");

    new CodeAdapter(adapterParams, confParameters)
        .adaptWithTopSeparation(
            referenceCD, concreteCD, Set.of("stud"), refCodePath, concrete, topOutput);

    String handwrittenStudent = readFileContent(topOutput, "Student.java");
    String adaptedTop = readFileContent(topOutput, "StudentTOP.java");
    assertTrue(handwrittenStudent.contains("class Student extends StudentTOP"));
    assertTrue(adaptedTop.contains("class StudentTOP extends StudentTOPTOP"));
    assertTrue(adaptedTop.contains("printHiwiRoles()"));
    assertGeneratedJavaCompiles(topOutput);
  }
}
