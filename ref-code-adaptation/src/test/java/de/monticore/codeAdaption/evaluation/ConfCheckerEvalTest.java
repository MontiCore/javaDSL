package de.monticore.codeAdaption.evaluation;

import static de.monticore.cdconformance.CDConfParameter.*;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConfCheckerEvalTest extends EvaluationAbstractTest {
  @BeforeEach
  public void setup() {
    referenceCD = new File(resourcesPath + "confCheckingCD/Reference.cd");
    concreteCD = new File(resourcesPath + "confCheckingCD/Concrete.cd");
    refCodePath = Path.of(resourcesPath + "confCheckingCD/reference");
    conCodePath = Path.of(resourcesPath + "confCheckingCD/concrete");
    output = Path.of("target/codeAdapter/evaluation/confCheckingCD/");
    CD4CodeMill.init();
    Log.init();
    confParameters = Set.of(INHERITANCE, STEREOTYPE_MAPPING, SRC_TARGET_ASSOC_MAPPING);
  }

  @Test
  @DisplayName("Evaluation conformance checker for class diagrams")
  public void evaluationConformanceCheckerCDtest() {
    long start = System.currentTimeMillis();
    Set<String> mappings = Set.of("prof", "stud");
    ASTCDCompilationUnit conCD = JavaLoader.loadCD(concreteCD);
    ASTCDCompilationUnit refCD = JavaLoader.loadCD(referenceCD);

    CDConformanceChecker conformanceChecker = new CDConformanceChecker(confParameters);
    Assertions.assertTrue(conformanceChecker.checkConformance(conCD, refCD, mappings));
    double duration = (System.currentTimeMillis() - start) / 1000.0;
    System.out.println("duration: " + duration);
  }
}
