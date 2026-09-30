/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.util.JavaSourceTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.*;

public class UnparsableModelsTest extends AbstractTest {
  

  @JavaSourceTest(basePath = "src/test/resources/unparsableModels", files = {
      "BasicCompilationUnitMissingBracket.java",
      "TwoTimesClass.java",
      "WrongExpression.java",
      "WrongIdentifierName.java"
  })
  public void testUnparsableModels(Path path) {
    assertParsingFailure(path);
  }
}
