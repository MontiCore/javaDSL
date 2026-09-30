/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

/**
 * Parameterized test over Java source files. Each file ({@code basePath} + {@code files}) is passed
 * as a {@link String} path to the test method. If {@code files} is empty, all {@code .java} files
 * in {@code basePath} and its subfolders are used. May be repeated to combine several base paths.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(JavaSourceTests.class)
@ParameterizedTest
@ArgumentsSource(JavaSourceArgumentsProvider.class)
public @interface JavaSourceTest {

  String basePath();

  String[] files() default {};
}
