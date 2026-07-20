package de.monticore.codeAdaption.utils;

/** Configures matcher precedence and the policy for Java elements that no strategy matches. */
public enum AdapterParam {
  /** Enables convention-based matching of equal Java and reference-CD names. */
  NAME_MATCHING,
  /** Enables matching when a Java identifier contains a reference-CD element name. */
  INFIX_MATCHING,
  /** Enables explicit matching through {@link Adapt} metadata. */
  ANNOTATION_MATCHING,

  /**
   * Accepts an otherwise unmatched Java type instead of reporting a validation error.
   *
   * <p>The type has no reference-CD incarnation to adapt and is therefore excluded from
   * mapping-specific adapted output. Use an explicit {@code @Adapt(ignore = true)} annotation when
   * an intentionally unchanged reference type must remain in that output.
   */
  IGNORE_NON_MATCHED_TYPE,

  /** Leaves an otherwise unmatched local variable or parameter unchanged. */
  IGNORE_NON_MATCHED_VAR,

  /** Leaves an otherwise unmatched field, method, or supertype declaration unchanged. */
  IGNORE_NON_MATCHED_TYPE_MEMBER
}
