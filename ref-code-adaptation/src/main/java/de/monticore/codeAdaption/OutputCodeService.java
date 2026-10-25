package de.monticore.codeAdaption;

import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdaterMill;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handles final generated-source cleanup and inclusion of pre-existing concrete handwritten files.
 *
 * <p>Adapted files are already present in the staging output. Concrete handwritten files are added
 * without overwriting the user's source tree. Multi-type Java units are split by declaration and
 * merged into an existing staged target with the staged output taking precedence.
 */
final class OutputCodeService {

  /**
   * Copies concrete handwritten files into their final package-relative output locations.
   *
   * <p>Java files are placed according to their declared package rather than their input folder.
   * For example, {@code misc/Port.java} declaring {@code package shipping;} targets {@code
   * shipping/Port.java}. Non-Java files preserve their path relative to {@code conHwcPath}. If two
   * inputs target the same output path, identical single-file content is deduplicated and
   * overlapping declarations are rejected.
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
      Map<Path, ASTOrdinaryCompilationUnit> splitJavaByTarget = new LinkedHashMap<>();
      Map<Path, Path> ownersByTarget = new LinkedHashMap<>();
      for (Path source :
          paths
              .filter(Files::isRegularFile)
              .sorted(java.util.Comparator.comparing(Path::toString))
              .toList()) {
        if (source.toString().endsWith(".java")) {
          ASTOrdinaryCompilationUnit unit = loadConcreteJava(source);
          if (unit.getTypeDeclarationList().size() > 1) {
            for (ASTTypeDeclaration type : unit.getTypeDeclarationList()) {
              ASTOrdinaryCompilationUnit split = compilationUnitForType(unit, type.getName());
              Path target =
                  concreteTypeTarget(split, type.getName(), outputPath)
                      .toAbsolutePath()
                      .normalize();
              registerUniqueTarget(ownersByTarget, target, source);
              splitJavaByTarget.put(target, split);
            }
            continue;
          }
        }
        Path target = concreteCopyTarget(conHwcPath, source, outputPath).toAbsolutePath().normalize();
        Path existingSource = ownersByTarget.putIfAbsent(target, source);
        if (existingSource != null) {
          if (!splitJavaByTarget.containsKey(target)
              && Files.mismatch(existingSource, source) == -1L) {
            continue;
          }
          throw targetCollision(existingSource, source, target);
        }
        sourcesByTarget.put(target, source);
      }
      for (Map.Entry<Path, Path> entry : sourcesByTarget.entrySet()) {
        Path target = entry.getKey();
        Path source = entry.getValue();
        Files.createDirectories(target.getParent());
        if (!Files.exists(target)) {
          Files.copy(source, target);
        }
      }
      for (Map.Entry<Path, ASTOrdinaryCompilationUnit> entry : splitJavaByTarget.entrySet()) {
        Path target = entry.getKey();
        Files.createDirectories(target.getParent());
        ASTOrdinaryCompilationUnit concreteFragment = entry.getValue();
        ASTOrdinaryCompilationUnit finalUnit =
            Files.exists(target)
                ? AdapterUtils.mergeAstsPreferringLeft(
                    JavaLoader.loadJava(target.toFile()), concreteFragment)
                : concreteFragment;
        JavaLoader.printAST(Set.of(finalUnit), outputPath);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to include concrete handwritten code", e);
    }
  }

  /** Removes adapter-only annotations/imports and performs the updater's final source cleanup. */
  void cleanCode(Path codePath) {
    cleanCode(codePath, Map.of());
  }

  /** Cleans final Java and applies self-type bindings recorded by TOP composition. */
  void cleanCode(Path codePath, Map<String, String> topToPublicSelfTypes) {
    CodeUpdaterMill.reset();
    try {
      CodeUpdater updater = CodeUpdaterMill.getUpdater();
      updater.cleanCode(codePath, topToPublicSelfTypes);
    } finally {
      CodeUpdaterMill.reset();
    }
  }

  private ASTOrdinaryCompilationUnit loadConcreteJava(Path source) {
    try {
      return JavaLoader.loadJava(source.toFile());
    } catch (RuntimeException | AssertionError parseFailure) {
      throw new IllegalStateException(
          "Failed to determine declarations in concrete Java file " + source, parseFailure);
    }
  }

  private ASTOrdinaryCompilationUnit compilationUnitForType(
      ASTOrdinaryCompilationUnit source, String typeName) {
    ASTOrdinaryCompilationUnit split = source.deepClone();
    for (ASTTypeDeclaration type :
        new java.util.ArrayList<>(split.getTypeDeclarationList())) {
      if (!type.getName().equals(typeName)) {
        split.removeTypeDeclaration(type);
      }
    }
    return split;
  }

  private Path concreteTypeTarget(
      ASTOrdinaryCompilationUnit unit, String typeName, Path outputPath) {
    if (unit.isPresentPackageDeclaration()) {
      Path packagePath =
          Path.of(
              unit.getPackageDeclaration()
                  .getMCQualifiedName()
                  .getQName()
                  .replace('.', File.separatorChar));
      return outputPath.resolve(packagePath).resolve(typeName + ".java");
    }
    return outputPath.resolve(typeName + ".java");
  }

  private void registerUniqueTarget(
      Map<Path, Path> ownersByTarget, Path target, Path source) {
    Path existingSource = ownersByTarget.putIfAbsent(target, source);
    if (existingSource != null) {
      throw targetCollision(existingSource, source, target);
    }
  }

  private IllegalStateException targetCollision(
      Path existingSource, Path source, Path target) {
    return new IllegalStateException(
        "Concrete files '"
            + existingSource
            + "' and '"
            + source
            + "' both target '"
            + target
            + "' but declare overlapping Java output");
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
