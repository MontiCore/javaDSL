package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.ADAPTED_NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.NAME_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_TYPE;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.CodeAdapter;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssociationPatternAdaptationTest extends EvaluationAbstractTest {

  @BeforeEach
  void setup() {
    initMills();
    configureFixture("association_role_usage_mi");
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, IGNORE_NON_MATCHED_VAR, IGNORE_NON_MATCHED_TYPE);
    confParameters =
        Set.of(
            STEREOTYPE_MAPPING,
            NAME_MAPPING,
            ADAPTED_NAME_MAPPING,
            SRC_TARGET_ASSOC_MAPPING,
            INHERITANCE);
  }

  @Test
  void adaptsMultiIncarnationAssociationRoleUsage() {
    deleteRecursively(output);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                referenceCD,
                concreteCD,
                Set.of("ref"),
                refCodePath,
                conCodePath,
                output,
                true,
                true));

    String adaptedTeacher = readFileContent(output, "Teacher.java");
    assertTrue(adaptedTeacher.contains("firstMathDept(MathDept fallback)"));
    assertTrue(adaptedTeacher.contains("firstScienceDept(ScienceDept fallback)"));
    assertTrue(adaptedTeacher.contains("for (MathDept department : this.mathDepts)"));
    assertTrue(
        adaptedTeacher.contains(
            "for (ScienceDept department : this.scienceDepts)"));
    assertFalse(adaptedTeacher.contains("this.departments)"));

    String generatedTeacherTop = readFileContent(output, "TeacherTOP.java");
    assertTrue(generatedTeacherTop.contains("Set<MathDept> mathDepts"));
    assertTrue(generatedTeacherTop.contains("Set<ScienceDept> scienceDepts"));

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);
  }

  @Test
  void adaptsBidirectionalAssociationRoleWritesOnBothOwners() {
    configureFixture("association_bidirectional_role_write");
    deleteRecursively(output);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);

    assertDoesNotThrow(
        () ->
            adapter.adapt(
                referenceCD,
                concreteCD,
                Set.of("ref"),
                refCodePath,
                conCodePath,
                output,
                true,
                true));

    String adaptedSprint = readFileContent(output, "Sprint.java");
    assertTrue(adaptedSprint.contains("replaceTicket(Ticket replacement)"));
    assertTrue(adaptedSprint.contains("this.tickets = replacement"));
    assertFalse(adaptedSprint.contains("this.tasks"));

    String adaptedTicket = readFileContent(output, "Ticket.java");
    assertTrue(adaptedTicket.contains("moveToSprint(Sprint replacement)"));
    assertTrue(adaptedTicket.contains("this.sprint = replacement"));
    assertFalse(adaptedTicket.contains("this.project"));

    String generatedSprintTop = readFileContent(output, "SprintTOP.java");
    assertTrue(generatedSprintTop.contains("Ticket tickets"));
    String generatedTicketTop = readFileContent(output, "TicketTOP.java");
    assertTrue(generatedTicketTop.contains("Sprint sprint"));

    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);
  }

  private void configureFixture(String fixture) {
    referenceCD = new File(resourcesPath + fixture + "/Reference.cd");
    concreteCD = new File(resourcesPath + fixture + "/Concrete.cd");
    refCodePath = Path.of(resourcesPath + fixture + "/adapter");
    conCodePath = Path.of(resourcesPath + fixture + "/concrete");
    output = Path.of("target/codeAdapter/evaluation/" + fixture);
  }
}
