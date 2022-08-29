/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.api.Test;

import static de.monticore.java.JavaDSLAssertions.*;

public class UnparsableModelsTest extends AbstractTest {

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
