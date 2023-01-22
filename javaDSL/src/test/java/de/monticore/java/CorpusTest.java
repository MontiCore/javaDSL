/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.monticore.java.JavaDSLAssertions.*;

/**
 * Performs tests on a large corpus of open-source libraries. The source
 * artifacts are downloaded using Gradle's dependency mechanism and prepared by
 * the {@code extractCorpus} task.
 *
 * <p>To add or remove libraries to the corpus, add a dependency to the "corpus"
 * configuration in the build script.</p>
 */
public final class CorpusTest extends AbstractTest {

  private static List<String> provideFilesForCorpusTests() throws IOException {
    try (Stream<Path> stream = Files.walk(Path.of("target/corpus"))) {
      return stream.map(path -> Files.isRegularFile(path) ? path.toAbsolutePath().toString() : null)
          .filter(Objects::nonNull)
          .collect(Collectors.toUnmodifiableList());
    }
  }

  @ParameterizedTest
  @MethodSource("provideFilesForCorpusTests")
  public void testParsing(String path) {
    assertParsingSuccess(path);
  }

}