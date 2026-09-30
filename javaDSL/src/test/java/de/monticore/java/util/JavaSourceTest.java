/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

/**
 * Parameterized test over Java source files. Each file ({@code basePath} + {@code files}) is passed
 * as a {@link java.nio.file.Path} to the test method. If {@code files} is empty, all {@code .java}
 * files in {@code basePath} and its subfolders are used. May be repeated to combine several base
 * paths.
 *
 * <p>The test fails early if the base path or one of the listed files does not exist, or if no
 * file is found at all, so that a typo cannot silently shrink the test set.</p>
 *
 * @see TestModels for commonly used base paths
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(JavaSourceTests.class)
@ParameterizedTest(name = "{0}")
@ArgumentsSource(JavaSourceArgumentsProvider.class)
public @interface JavaSourceTest {

  String basePath();

  String[] files() default {};

  /**
   * Files relative to {@code basePath} that are skipped, e.g., models of known but unfixed bugs.
   * Each excluded file must exist.
   */
  String[] exclude() default {};
}
