/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.runtime.junit.TestWithMCLanguage;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingFailure;

@TestWithMCLanguage(JavaDSLMill.class)
public class UnparsableModelsTest {
  

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
