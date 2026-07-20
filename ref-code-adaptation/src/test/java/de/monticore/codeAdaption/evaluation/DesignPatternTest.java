package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.INHERITANCE;
import static de.monticore.cdconformance.CDConfParameter.SRC_TARGET_ASSOC_MAPPING;
import static de.monticore.cdconformance.CDConfParameter.STEREOTYPE_MAPPING;
import static de.monticore.codeAdaption.utils.AdapterParam.ANNOTATION_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;
import static de.monticore.codeAdaption.utils.AdapterParam.INFIX_MATCHING;
import static de.monticore.codeAdaption.utils.AdapterParam.NAME_MATCHING;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.CodeAdapter;
import de.monticore.codeAdaption.testutil.CD4CodeTestGenerator;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DesignPatternTest extends EvaluationAbstractTest {
  @BeforeEach
  public void setup() {
    initMills();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    confParameters = Set.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
  }

  @Test
  @DisplayName("Evaluation paper example: Composite pattern")
  public void adaptsCompositePatternForBothMappings() {
    configureFixture("Composition", "composition");

    adaptAndGenerate(Set.of("ci", "re"));

    assertTrue(
        generatedFileNames(output)
            .containsAll(Set.of("Circle.java", "Graphic.java", "Picture.java", "Rectangle.java")));
    String picture = readFileContent(output, "Picture.java");
    assertTrue(picture.contains("class Picture implements Graphic"));
    assertTrue(picture.contains("void render()"));
    assertTrue(picture.contains("List<Graphic> graphicList"));
    assertFalse(picture.contains("Component"));
    assertFalse(picture.contains("execute("));
    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);
  }

  @Test
  @DisplayName("Evaluation paper example: Adapter pattern")
  public void adaptsAdapterPattern() {
    configureFixture("Adapter", "adapter");

    adaptAndGenerate(Set.of("npg"));

    assertTrue(
        generatedFileNames(output)
            .containsAll(Set.of("FlexImage.java", "NPGAdapter.java", "NPGImage.java")));
    String adapter = readFileContent(output, "NPGAdapter.java");
    assertTrue(adapter.contains("class NPGAdapter implements FlexImage"));
    assertTrue(adapter.contains("void flex()"));
    assertFalse(adapter.contains("Target"));
    assertFalse(adapter.contains("operation("));
    assertNoAdapterMetadata(output);
    assertGeneratedJavaCompiles(output);
  }

  private void configureFixture(String referenceName, String codeDirectory) {
    referenceCD = new File(resourcesPath + "design_patterns/" + referenceName + ".cd");
    concreteCD = new File(resourcesPath + "design_patterns/DesignPatterns.cd");
    refCodePath = Path.of(resourcesPath + "design_patterns/hwc/" + codeDirectory);
    conCodePath = Path.of(resourcesPath + "design_patterns/concrete");
    output = Path.of("target/codeAdapter/evaluation/design_patterns/" + codeDirectory);
  }

  private void adaptAndGenerate(Set<String> mappings) {
    deleteRecursively(output);
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    assertDoesNotThrow(
        () -> adapter.adapt(referenceCD, concreteCD, mappings, refCodePath, conCodePath, output));
    assertDoesNotThrow(
        () -> CD4CodeTestGenerator.generate(concreteCD, output, output.resolve("generated")));
  }
}
