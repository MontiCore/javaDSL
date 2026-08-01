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
import org.junit.jupiter.api.io.TempDir;

public class CodeAdapterTestCase2a extends EvaluationAbstractTest {

  @TempDir Path tempDir;

  @BeforeEach
  public void setup() {
    referenceCD = new File(resourcesPath + "testcase_2_cd4code/Reference.cd");
    concreteCD = new File(resourcesPath + "testcase_2_cd4code/Concrete.cd");
    refCodePath = Path.of(resourcesPath + "testcase_2_cd4code/hwc/entity");
    conCodePath = Path.of(resourcesPath + "testcase_2_cd4code/concrete");
    output = Path.of("target/codeAdapter/evaluation/testcase_2_cd4code/");
    CD4CodeMill.init();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    confParameters = Set.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 2 CD4Code")
  public void adaptsAssociationRoleUsageAndGeneratesRoleField() {
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

    String generatedStudentTop = readFileContent(output, "StudentTOP.java");
    assertTrue(generatedStudentTop.contains("abstract class StudentTOP"));
    assertTrue(generatedStudentTop.contains("Set<HiwiRole> roles"));
    assertFalse(generatedStudentTop.contains("Set<Role>"));

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);
  }

  @Test
  void suppliedJavaEmptyConcreteDirectoryTriggersGenerationAndPreservesResources()
      throws IOException {
    Path emptyConcrete = Files.createDirectories(tempDir.resolve("concrete"));
    Files.writeString(emptyConcrete.resolve("settings.txt"), "preserve-me");
    Path generatedOutput = tempDir.resolve("generated-with-resource");

    new CodeAdapter(adapterParams, confParameters)
        .adapt(
            referenceCD,
            concreteCD,
            Set.of("stud"),
            refCodePath,
            emptyConcrete,
            generatedOutput);

    assertTrue(Files.isRegularFile(generatedOutput.resolve("Concrete/StudentTOP.java")));
    assertTrue(Files.isRegularFile(generatedOutput.resolve("settings.txt")));
    assertGeneratedJavaCompiles(generatedOutput);
  }

  @Test
  void emptyReferenceDirectoryStillGeneratesConcreteModel() throws IOException {
    Path emptyReference = Files.createDirectories(tempDir.resolve("empty-reference"));
    Path generatedOutput = tempDir.resolve("model-only-output");

    new CodeAdapter(adapterParams, confParameters)
        .adaptWithoutConcreteCode(
            referenceCD, concreteCD, Set.of("stud"), emptyReference, generatedOutput);

    assertTrue(generatedJavaFiles(generatedOutput).stream().anyMatch(path -> path.endsWith("Student.java")));
    assertTrue(generatedJavaFiles(generatedOutput).stream().anyMatch(path -> path.endsWith("HiwiRole.java")));
    assertGeneratedJavaCompiles(generatedOutput);
  }
}
