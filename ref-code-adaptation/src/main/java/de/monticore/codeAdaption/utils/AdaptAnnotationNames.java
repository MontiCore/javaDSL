package de.monticore.codeAdaption.utils;

/** Compares annotation AST names with the supported simple and qualified {@link Adapt} names. */
public final class AdaptAnnotationNames {

  private AdaptAnnotationNames() {}

  /** Returns whether the supplied AST qualified name denotes {@link Adapt}. */
  public static boolean matches(String qualifiedName) {
    return Constants.ANNOT_NAME.equals(qualifiedName)
        || Constants.ANNOT_PACKAGE.equals(qualifiedName);
  }
}
