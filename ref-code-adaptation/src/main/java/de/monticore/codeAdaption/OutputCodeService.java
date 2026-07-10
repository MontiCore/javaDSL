package de.monticore.codeAdaption;

import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdaterMill;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Handles final generated-source cleanup and concrete handwritten-file inclusion. */
final class OutputCodeService {

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
      paths
          .filter(Files::isRegularFile)
          .sorted(java.util.Comparator.comparing(Path::toString))
          .forEach(
              path -> {
                try {
                  Path target = concreteCopyTarget(conHwcPath, path, outputPath);
                  Files.createDirectories(target.getParent());
                  if (!Files.exists(target)) {
                    Files.copy(path, target);
                  }
                } catch (IOException e) {
                  throw new IllegalStateException(
                      "Failed to copy concrete file '" + path + "' to output", e);
                }
              });
    } catch (IOException e) {
      throw new IllegalStateException("Failed to include concrete handwritten code", e);
    }
  }

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
