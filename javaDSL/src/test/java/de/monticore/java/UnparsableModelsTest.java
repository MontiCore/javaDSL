/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static de.monticore.java.JavaDSLAssertions.*;

public class UnparsableModelsTest extends AbstractTest {
  
  @ParameterizedTest
  @ValueSource(strings = {
      "src/test/resources/unparsableModels/BasicCompilationUnitMissingBracket.java",
      "src/test/resources/unparsableModels/TwoTimesClass.java",
      "src/test/resources/unparsableModels/WrongExpression.java",
      "src/test/resources/unparsableModels/WrongIdentifierName.java"
  })
  public void testUnparsableModels(String path) {
    assertParsingFailure(path);
  }
}
