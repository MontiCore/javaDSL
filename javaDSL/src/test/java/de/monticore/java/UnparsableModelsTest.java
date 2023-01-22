/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.api.Test;

import static de.monticore.java.JavaDSLAssertions.*;

<<<<<<< HEAD
/**
 * A simple test for java tool.
 *
 */
public class UnparsableModelsTest extends AbstractTestClass {
=======
public class UnparsableModelsTest extends AbstractTest {
>>>>>>> b1632b992b7e88612c226e7c337d14e8cbff6536

  @Test
  public void testBasicCompilationUnitMissingBracket() {
    assertParsingFailure("src/test/resources/unparsableModels/BasicCompilationUnitMissingBracket.java");
  }

  @Test
  public void testTwoTimesClass() {
      assertParsingFailure("src/test/resources/unparsableModels/TwoTimesClass.java");
  }

  @Test
  public void testWrongExpression() {
      assertParsingFailure("src/test/resources/unparsableModels/WrongExpression.java");
  }

  @Test
  public void testWrongIdentifierName() {
      assertParsingFailure("src/test/resources/unparsableModels/WrongIdentifierName.java");
  }

}
