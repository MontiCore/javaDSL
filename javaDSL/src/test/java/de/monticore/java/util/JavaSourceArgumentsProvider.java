/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.support.AnnotationSupport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class JavaSourceArgumentsProvider implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(
      ParameterDeclarations parameters,
      ExtensionContext context) {
    return AnnotationSupport
        .findRepeatableAnnotations(context.getRequiredTestMethod(), JavaSourceTest.class)
        .stream()
        .flatMap(a -> a.files().length == 0
            ? listFiles(Paths.get(a.basePath()))
            : Arrays.stream(a.files()).map(file -> Paths.get(a.basePath()).resolve(file).toString()))
        .map(Arguments::of);
  }

  private static Stream<String> listFiles(Path dir) {
    try (Stream<Path> children = Files.walk(dir)) {
      return children
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(".java"))
          .map(Path::toString)
          .sorted()
          .toList()
          .stream();
    }
    catch (IOException e) {
      throw new UncheckedIOException("Cannot list files in " + dir, e);
    }
  }
}
