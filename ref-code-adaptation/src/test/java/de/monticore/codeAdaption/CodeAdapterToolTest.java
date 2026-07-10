package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CodeAdapterToolTest extends AdapterAbstractTest {

  private static final Path RESOURCES =
      Path.of("src/test/resources/de/monticore/codeAdaption");

  @BeforeEach
  void setUp() {
    initMills();
  }

  @Test
  void commandLineInvocationProducesAdaptedOutput() {
    Path output = Path.of("target/adapter/cli");
    deleteRecursively(output);

    int exitCode =
        new CodeAdapterTool()
            .execute(
                new String[] {
                  "--reference",
                  RESOURCES.resolve("App.cd").toString(),
                  "--concrete",
                  RESOURCES.resolve("UniApp.cd").toString(),
                  "--reference-code",
                  RESOURCES.resolve("adapter/name").toString(),
                  "--concrete-code",
                  RESOURCES.resolve("hwc").toString(),
                  "--output",
                  output.toString(),
                  "--mapping",
                  "ref",
                  "--matching",
                  "name"
                });

    assertEquals(0, exitCode);
    assertTrue(Files.exists(findGeneratedFile(output, "Student.java")));
    assertGeneratedJavaCompiles(output);
  }

  private static Path findGeneratedFile(Path output, String fileName) {
    try (var files = Files.walk(output)) {
      return files
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().equals(fileName))
          .findFirst()
          .orElse(output.resolve(fileName));
    } catch (java.io.IOException exception) {
      throw new IllegalStateException("Failed to inspect CLI output", exception);
    }
  }
}
