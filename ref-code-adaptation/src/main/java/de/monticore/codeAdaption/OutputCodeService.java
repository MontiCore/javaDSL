package de.monticore.codeAdaption;

import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdaterMill;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles final generated-source cleanup and inclusion of pre-existing concrete handwritten files.
 *
 * <p>Adapted files are already present in the staging output. Concrete handwritten files are added
 * only when no adapted file occupies their target path, so adapted output wins without overwriting
 * the user's concrete source tree.
 */
final class OutputCodeService {

  /**
   * Copies concrete handwritten files into their final package-relative output locations.
   *
   * <p>Java files are placed according to their declared package rather than their input folder.
   * For example, {@code misc/Port.java} declaring {@code package shipping;} targets {@code
   * shipping/Port.java}. Non-Java files preserve their path relative to {@code conHwcPath}. If two
   * inputs target the same output path, identical content is deduplicated and differing content is
   * rejected.
   */
  void copyConcreteFiles(Path conHwcPath, Path outputPath) {
    if (!Files.exists(conHwcPath)) {
      try {
        Files.createDirectories(outputPath);
      } catch (IOException e) {
        throw new IllegalStateException("Failed to create output directory " + outputPath, e);
      }
      return;
    }
    try (var paths = Files.walk(conHwcPath)) {
      Map<Path, Path> sourcesByTarget = new LinkedHashMap<>();
      for (Path source :
          paths
              .filter(Files::isRegularFile)
              .sorted(java.util.Comparator.comparing(Path::toString))
              .toList()) {
        Path target = concreteCopyTarget(conHwcPath, source, outputPath).toAbsolutePath().normalize();
        Path existingSource = sourcesByTarget.putIfAbsent(target, source);
        if (existingSource != null && Files.mismatch(existingSource, source) != -1L) {
          throw new IllegalStateException(
              "Concrete files '"
                  + existingSource
                  + "' and '"
                  + source
                  + "' both target '"
                  + target
                  + "' but have different content");
        }
      }
      for (Map.Entry<Path, Path> entry : sourcesByTarget.entrySet()) {
        Path target = entry.getKey();
        Path source = entry.getValue();
        Files.createDirectories(target.getParent());
        if (!Files.exists(target)) {
          Files.copy(source, target);
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to include concrete handwritten code", e);
    }
  }

  /** Removes adapter-only annotations/imports and performs the updater's final source cleanup. */
  void cleanCode(Path codePath) {
    CodeUpdaterMill.reset();
    try {
      CodeUpdater updater = CodeUpdaterMill.getUpdater();
      updater.cleanCode(codePath);
    } finally {
      CodeUpdaterMill.reset();
    }
  }

  private Path concreteCopyTarget(Path conHwcPath, Path source, Path outputPath) {
    if (!source.toString().endsWith(".java")) {
      return outputPath.resolve(conHwcPath.relativize(source));
    }
    try {
      ASTOrdinaryCompilationUnit ast = JavaLoader.loadJava(source.toFile());
      if (ast.isPresentPackageDeclaration()) {
        Path packagePath =
            Path.of(
                ast.getPackageDeclaration()
                    .getMCQualifiedName()
                    .getQName()
                    .replace('.', File.separatorChar));
        return outputPath.resolve(packagePath).resolve(source.getFileName());
      }
    } catch (RuntimeException | AssertionError parseFailure) {
      throw new IllegalStateException(
          "Failed to determine package for concrete Java file " + source, parseFailure);
    }
    return outputPath.resolve(conHwcPath.relativize(source));
  }
}
