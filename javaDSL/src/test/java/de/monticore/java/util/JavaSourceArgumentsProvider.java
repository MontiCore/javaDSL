/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Provides the Java source files described by {@link JavaSourceTest} annotations. Each file is
 * passed as a {@link Path} named after its location relative to the base path, which keeps the
 * test report readable.
 */
public class JavaSourceArgumentsProvider implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(
      ParameterDeclarations parameters,
      ExtensionContext context) {
    return AnnotationSupport
        .findRepeatableAnnotations(context.getRequiredTestMethod(), JavaSourceTest.class)
        .stream()
        .flatMap(JavaSourceArgumentsProvider::sources)
        .map(Arguments::of);
  }

  private static Stream<Named<Path>> sources(JavaSourceTest annotation) {
    Path basePath = Path.of(annotation.basePath());
    if (!Files.isDirectory(basePath)) {
      throw new PreconditionViolationException(
          "@JavaSourceTest base path is not a directory: " + basePath.toAbsolutePath());
    }

    List<Path> excluded = resolveExisting(basePath, annotation.exclude());
    List<Path> files = (annotation.files().length == 0
        ? listJavaFiles(basePath)
        : resolveExisting(basePath, annotation.files()))
        .stream()
        .filter(file -> !excluded.contains(file))
        .toList();

    if (files.isEmpty()) {
      throw new PreconditionViolationException(
          "@JavaSourceTest found no Java files in " + basePath.toAbsolutePath());
    }

    return files.stream().map(file -> Named.of(displayName(basePath, file), file));
  }

  private static List<Path> resolveExisting(Path basePath, String[] relativePaths) {
    List<Path> files = Arrays.stream(relativePaths).map(basePath::resolve).toList();
    for (Path file : files) {
      if (!Files.isRegularFile(file)) {
        throw new PreconditionViolationException(
            "@JavaSourceTest file does not exist: " + file.toAbsolutePath());
      }
    }
    return files;
  }

  private static String displayName(Path basePath, Path file) {
    return basePath.relativize(file).toString().replace('\\', '/');
  }

  private static List<Path> listJavaFiles(Path dir) {
    try (Stream<Path> children = Files.walk(dir)) {
      return children
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(".java"))
          .sorted()
          .toList();
    }
    catch (IOException e) {
      throw new UncheckedIOException("Cannot list files in " + dir, e);
    }
  }
}
