/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.runtime.junit.TestWithMCLanguage;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;

/**
 * Performs tests on a large corpus of open-source libraries. The source
 * artifacts are downloaded using Gradle's dependency mechanism and prepared by
 * the {@code extractCorpus} task.
 *
 * <p>To add or remove libraries to the corpus, add a dependency to the "corpus"
 * configuration in the build script.</p>
 */
@TestWithMCLanguage(JavaDSLMill.class)
public final class CorpusTest {

  /*
   * To learn more about corpus tests, please refer to:
   * /src/main/grammars/de/monticore/java/JavaDSL.md
   */

  @JavaSourceTest(basePath = "target/corpus/guava-31.1-jre-sources")
  @JavaSourceTest(basePath = "target/corpus/monticore-runtime-7.8.0-sources")
  public void testParsing(Path path) {
    assertParsingSuccess(path);
  }

}