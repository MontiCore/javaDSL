/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.util.TestModels;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@TestWithMCLanguage(JavaDSLMill.class)
public class JavaDSLToolTest {

  @TempDir
  private Path outputDir;

  /** Input model and the files (relative to the output directory) the tool must generate. */
  static Stream<Arguments> testTool() {
    return Stream.of(
        arguments(
            Path.of(TestModels.PARSER, "Test.java"),
            List.of(Path.of("de", "monticore", "foo", "bla", "Test.java"))),
        arguments(
            Path.of(TestModels.PARSER, "ASTClassDeclaration.java"),
            List.of(
                Path.of("de", "monticore", "java", "javadsl", "_ast", "ASTClassDeclaration.java"),
                Path.of("de", "monticore", "java", "javadsl", "_ast", "Builder.java")))
    );
  }

  @ParameterizedTest
  @MethodSource
  public void testTool(Path inputPath, List<Path> expectedOutputs) throws IOException {
    List<String> toolArgs = new ArrayList<>(
        List.of("-i", inputPath.toString(), "-o", outputDir.toString(), "-c2mc"));
    for (String classpathEntry : System.getProperty("java.class.path").split(File.pathSeparator)) {
      toolArgs.add("-path");
      toolArgs.add(classpathEntry);
    }

    JavaDSLTool.main(toolArgs.toArray(String[]::new));

    MCAssertions.assertNoFindings("The tool reported findings");
    for (Path relativeOutput : expectedOutputs) {
      Path output = outputDir.resolve(relativeOutput);
      assertTrue(Files.isRegularFile(output), "could not find generated file: " + output);
      assertTrue(Files.size(output) > 0, "generated file is empty: " + output);
    }
  }
}
