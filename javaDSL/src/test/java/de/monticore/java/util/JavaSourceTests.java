/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

/** Container for repeated {@link JavaSourceTest} annotations. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ParameterizedTest(name = "{0}")
@ArgumentsSource(JavaSourceArgumentsProvider.class)
public @interface JavaSourceTests {

  JavaSourceTest[] value();
}
