/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.TestModels;
import de.monticore.runtime.junit.TestWithMCLanguage;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;

/** Every model in {@link TestModels#PARSABLE_AND_COMPILABLE} must be parsable. */
@TestWithMCLanguage(JavaDSLMill.class)
public class ParsableAndCompilableModelsTest {

  @JavaSourceTest(basePath = TestModels.PARSABLE_AND_COMPILABLE, exclude = {
      // TODO: the parser does not accept 'record' as an identifier
      "parserBugs/RecordFields.java"
  })
  public void testParsableAndCompilableModels(Path path) {
    assertParsingSuccess(path);
  }
}
