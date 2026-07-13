package de.monticore.codeAdaption;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.IOException;
import java.nio.file.Files;
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

  @Test
  void rejectsDifferentConcreteFilesWithTheSamePackageTarget() throws IOException {
    Path concrete = tempDir.resolve("concrete");
    Path first = concrete.resolve("first/User.java");
    Path second = concrete.resolve("second/User.java");
    Files.createDirectories(first.getParent());
    Files.createDirectories(second.getParent());
    Files.writeString(first, "package p; class User { int first; }");
    Files.writeString(second, "package p; class User { int second; }");

    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class,
            () -> new OutputCodeService().copyConcreteFiles(concrete, tempDir.resolve("output")));

    assertTrue(exception.getMessage().contains("both target"));
  }
}
