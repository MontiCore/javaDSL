/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import java.io.IOException;

import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

/**
 * A simple test for java tool.
 *
 * @author (last commit) $Author$
 */
public class UnparsableModelsTest extends AbstractTestClass {

  @Test
  public void testBasicCompilationUnitMissingBracket() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/BasicCompilationUnitMissingBracket.java");
  }

  @Test
  public void testTwoTimesClass() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/TwoTimesClass.java");
  }

  @Test
  public void testWrongExpression() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/WrongExpression.java");
  }

  @Test
  public void testWrongIdentifierName() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/WrongIdentifierName.java");
  }

}
