package de.monticore.codeAdaption.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares how a handwritten Java element relates to reference-CD elements.
 *
 * <p>The annotation may be placed on types, fields, methods, parameters, and local variables.
 * Explicit annotation matching takes precedence when {@link AdapterParam#ANNOTATION_MATCHING} is
 * enabled. Templates consume references from left to right using {@code ${}}, {@code
 * ${cap_first}}, and {@code ${uncap_first}} placeholders.
 */
@Target({
  ElementType.LOCAL_VARIABLE,
  ElementType.TYPE,
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
})
public @interface Adapt {
  /** Returns whether the annotated Java element is intentionally excluded from adaptation. */
  boolean ignore() default false;

  /**
   * Returns the qualified or owner-relative reference-CD element names represented by this Java
   * element.
   */
  String[] ref() default {};

  /**
   * Returns the name template used to validate and adapt the existing handwritten element.
   */
  String template() default "";

  /**
   * Returns the optional template used when a new name or declaration is generated.
   *
   * <p>For example, {@code template="${}"} validates an adapter type while {@code
   * genTemplate="${}Builder"} generates {@code PersonBuilder}. An empty value reuses the ordinary
   * template where generation supports that fallback.
   */
  String genTemplate() default "";
}
