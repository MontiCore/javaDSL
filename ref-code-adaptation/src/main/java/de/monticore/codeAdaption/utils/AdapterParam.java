package de.monticore.codeAdaption.utils;

/** Configures matcher precedence and the policy for Java elements that no strategy matches. */
public enum AdapterParam {
  /** Enables convention-based matching of equal Java and reference-CD names. */
  NAME_MATCHING,
  /** Enables matching when a Java identifier contains a reference-CD element name. */
  INFIX_MATCHING,
  /** Enables explicit matching through {@link Adapt} metadata. */
  ANNOTATION_MATCHING,

  /** Keeps an otherwise unmatched Java type instead of reporting a validation error. */
  IGNORE_NON_MATCHED_TYPE,

  /** Keeps an otherwise unmatched local variable or parameter. */
  IGNORE_NON_MATCHED_VAR,

  /** Keeps an otherwise unmatched field, method, or supertype declaration. */
  IGNORE_NON_MATCHED_TYPE_MEMBER
}
