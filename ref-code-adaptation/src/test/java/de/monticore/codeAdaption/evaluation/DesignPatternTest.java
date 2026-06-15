package de.monticore.codeAdaption.evaluation;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.codeAdaption.CodeAdapter;
import de.monticore.codeAdaption.utils.Generator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import static de.monticore.cdconformance.CDConfParameter.*;
import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static de.monticore.codeAdaption.utils.AdapterParam.IGNORE_NON_MATCHED_VAR;

public class DesignPatternTest extends EvaluationAbstractTest {
  @BeforeEach
  public void setup() {
    referenceCD = new File(resourcesPath + "design_patterns/Composition.cd");
    concreteCD = new File(resourcesPath + "design_patterns/Graphic.cd");
    refCodePath = Path.of(resourcesPath + "design_patterns/hwc/");
    conCodePath = Path.of(resourcesPath + "design_patterns/concrete");
    output = Path.of("target/codeAdapter/evaluation/design_patterns/");
    CD4CodeMill.init();
    adapterParams =
        Set.of(NAME_MATCHING, ANNOTATION_MATCHING, INFIX_MATCHING, IGNORE_NON_MATCHED_VAR);
    confParameters = Set.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
  }

  @Test
  @DisplayName("Evaluation Code Adapter Case Study 2 CD4Code")
  public void evaluationCodeAdapterCaseStudy1Test() {
    Set<String> mappings = Set.of("ci","re");
    CodeAdapter adapter = new CodeAdapter(adapterParams, confParameters);
    adapter.adapt(referenceCD, concreteCD, mappings, refCodePath, conCodePath, output);

    Path genCode = Path.of("target/codeAdapter/evaluation/design_patterns/generated");
    Generator.generate(concreteCD, output, genCode);
  }
}
