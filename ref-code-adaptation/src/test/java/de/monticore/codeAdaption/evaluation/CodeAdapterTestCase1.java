package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.CodeAdapter;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CodeAdapterTestCase1 extends EvaluationAbstractTest {

  @BeforeEach
  public void setup() {
    referenceCD = new File(resourcesPath + "testcase_1/Reference.cd");
    concreteCD = new File(resourcesPath + "testcase_1/Concrete.cd");
    refCodePath = Path.of(resourcesPath + "testcase_1/reference");
    conCodePath = Path.of(resourcesPath + "testcase_1/concrete");
    output = Path.of("target/codeAdapter/evaluation/testcase_1/");
    initMills();
    deleteRecursively(output);
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    confParameters = Set.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 1")
  public void evaluationCodeAdapterCaseStudy1Test() {
    Set<String> mappings = Set.of("stud", "prof");
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    assertDoesNotThrow(
        () -> adapter.adapt(referenceCD, concreteCD, mappings, refCodePath, conCodePath, output));
    assertFalse(generatedJavaFilesRecursively(output).isEmpty());
    assertNoAdapterMetadata(output);
    assertTrue(generatedFileNames(output).contains("ProfessorRegistration.java"));
    assertTrue(generatedFileNames(output).contains("StudentRegistration.java"));
    assertTrue(
        readFileContent(output, "ProfessorRegistration.java")
            .contains("register(Professor professor)"));
    assertTrue(
        readFileContent(output, "StudentRegistration.java")
            .contains("register(Student student)"));
    assertGeneratedJavaCompiles(output);
  }
}
