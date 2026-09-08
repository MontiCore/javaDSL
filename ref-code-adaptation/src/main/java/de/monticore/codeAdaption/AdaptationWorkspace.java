package de.monticore.codeAdaption;

import de.monticore.codeAdaption.utils.JavaLoader;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

/** Owns normalized paths, temporary directories, and transactional output publication. */
final class AdaptationWorkspace {

  private final Path referenceSource;
  private final Optional<Path> concreteSource;
  private final Path output;

  AdaptationWorkspace(Path referenceSource, Path concreteSource, Path output) {
    this(
        referenceSource,
        Optional.of(Objects.requireNonNull(concreteSource, "concreteSource")),
        output);
  }

  AdaptationWorkspace(Path referenceSource, Optional<Path> concreteSource, Path output) {
    this.referenceSource = canonicalPath(referenceSource);
    this.concreteSource =
        Objects.requireNonNull(concreteSource, "concreteSource")
            .map(AdaptationWorkspace::canonicalPath);
    this.output = canonicalPath(output);
    validatePaths(this.referenceSource, this.concreteSource, this.output);
  }

  Path referenceSource() {
    return referenceSource;
  }

  Optional<Path> concreteSourceOptional() {
    return concreteSource;
  }

  Path output() {
    return output;
  }

  void validateReadOnlyInput(Path input, String label) {
    rejectOverlap(label, canonicalPath(input), output);
  }

  Path createStagingDirectory() {
    try {
      Files.createDirectories(output.getParent());
      return Files.createTempDirectory(output.getParent(), ".code-adaptation-staging-");
    } catch (IOException exception) {
      throw new CodeAdaptationException("Could not create output staging directory", exception);
    }
  }

  Path createMappingDirectory(Path stagingDirectory) {
    try {
      Path directory = Files.createTempDirectory(stagingDirectory, ".mapping-");
      requireContained(stagingDirectory, directory);
      return directory;
    } catch (IOException exception) {
      throw new CodeAdaptationException("Could not create mapping workspace", exception);
    }
  }

  void discard(Path directory) {
    if (directory != null && Files.exists(directory)) {
      JavaLoader.removeDirectory(directory);
    }
  }

  void publish(Path stagingDirectory) {
    requireContained(output.getParent(), stagingDirectory);
    Path backup = null;
    try {
      if (Files.exists(output)) {
        backup = Files.createTempDirectory(output.getParent(), ".code-adaptation-backup-");
        Files.delete(backup);
        moveDirectory(output, backup);
      }
      moveDirectory(stagingDirectory, output);
      if (backup != null) {
        JavaLoader.removeDirectory(backup);
      }
    } catch (IOException | RuntimeException exception) {
      if (backup != null && Files.exists(backup)) {
        try {
          if (Files.exists(output)) {
            moveDirectory(output, stagingDirectory);
          }
          if (!Files.exists(output)) {
            moveDirectory(backup, output);
          }
        } catch (IOException restoreFailure) {
          exception.addSuppressed(restoreFailure);
        }
      }
      throw new CodeAdaptationException("Could not publish adapted code to " + output, exception);
    }
  }

  static void validatePaths(Path referenceSource, Path concreteSource, Path output) {
    validatePaths(
        referenceSource,
        Optional.of(Objects.requireNonNull(concreteSource, "concreteSource")),
        output);
  }

  static void validatePaths(Path referenceSource, Optional<Path> concreteSource, Path output) {
    Path normalizedReference = normalizedPath(referenceSource);
    Optional<Path> normalizedConcrete =
        Objects.requireNonNull(concreteSource, "concreteSource")
            .map(AdaptationWorkspace::normalizedPath);
    Path normalizedOutput = normalizedPath(output);
    if (normalizedOutput.getParent() == null) {
      throw new IllegalArgumentException(
          "Output path must not be a filesystem root: " + normalizedOutput);
    }
    rejectOverlap("reference handwritten code", normalizedReference, normalizedOutput);
    normalizedConcrete.ifPresent(
        concrete -> rejectOverlap("concrete handwritten code", concrete, normalizedOutput));
  }

  private static void rejectOverlap(String label, Path input, Path output) {
    if (input.equals(output) || input.startsWith(output) || output.startsWith(input)) {
      throw new IllegalArgumentException(
          "Output path must not overlap " + label + ": " + output + " and " + input);
    }
  }

  private static Path normalizedPath(Path path) {
    return Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
  }

  private static Path canonicalPath(Path path) {
    Path normalized = normalizedPath(path);
    Path existing = normalized;
    while (existing != null && !Files.exists(existing)) {
      existing = existing.getParent();
    }
    if (existing == null) {
      return normalized;
    }
    try {
      return existing.toRealPath().resolve(existing.relativize(normalized)).normalize();
    } catch (IOException exception) {
      throw new CodeAdaptationException("Could not resolve path " + normalized, exception);
    }
  }

  private static void requireContained(Path parent, Path child) {
    Path normalizedParent = normalizedPath(parent);
    Path normalizedChild = normalizedPath(child);
    if (!normalizedChild.startsWith(normalizedParent) || normalizedChild.equals(normalizedParent)) {
      throw new IllegalArgumentException(
          "Temporary path escapes its workspace: " + normalizedChild);
    }
  }

  private static void moveDirectory(Path source, Path target) throws IOException {
    try {
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException exception) {
      Files.move(source, target);
    }
  }
}
