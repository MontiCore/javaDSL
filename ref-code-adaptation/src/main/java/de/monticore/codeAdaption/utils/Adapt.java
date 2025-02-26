package de.monticore.codeAdaption.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({
  ElementType.LOCAL_VARIABLE,
  ElementType.TYPE,
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
})
public @interface Adapt {
  boolean ignore() default false;

  String[] ref() default {};

  String template() default "";
}
