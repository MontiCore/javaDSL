/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.TestModels;
import de.monticore.runtime.junit.TestWithMCLanguage;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingFailure;

/** No model in {@link TestModels#UNPARSABLE} may be parsable. */
@TestWithMCLanguage(JavaDSLMill.class)
public class UnparsableModelsTest {

  @JavaSourceTest(basePath = TestModels.UNPARSABLE)
  public void testUnparsableModels(Path path) {
    assertParsingFailure(path);
  }
}
