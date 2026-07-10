package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.monticore.codeAdaption.utils.JavaLoader;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodeAdapterSafetyTest {

  @TempDir Path tempDir;

  @Test
  void rejectsOutputInsideReferenceSources() {
    Path reference = tempDir.resolve("reference");
    Path concrete = tempDir.resolve("concrete");

    assertThrows(
        IllegalArgumentException.class,
        () -> CodeAdapter.validatePaths(reference, concrete, reference.resolve("generated")));
  }

  @Test
  void rejectsSourceInsideOutput() {
    Path output = tempDir.resolve("output");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            CodeAdapter.validatePaths(
                output.resolve("reference"), tempDir.resolve("concrete"), output));
  }

  @Test
  void rejectsClassDiagramInsideOutput() {
    Path output = tempDir.resolve("output");
    AdaptationWorkspace workspace =
        new AdaptationWorkspace(
            tempDir.resolve("reference"), tempDir.resolve("concrete"), output);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            workspace.validateReadOnlyInput(
                output.resolve("Reference.cd"), "reference class diagram"));
  }

  @Test
  void refusesToDeleteFilesystemRoot() {
    Path root = tempDir.toAbsolutePath().getRoot();

    assertThrows(IllegalArgumentException.class, () -> JavaLoader.removeDirectory(root));
  }

  @Test
  void missingJavaSourceDirectoryIsAnError() {
    assertThrows(
        IllegalArgumentException.class,
        () -> JavaLoader.readJavaCode(tempDir.resolve("does-not-exist")));
  }
}
