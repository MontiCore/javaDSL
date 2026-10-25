package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.List;
import java.util.Optional;

/**
 * Provides null-safe, consistently rendered access to CD relationships whose generated AST APIs
 * vary by type kind or grammar version.
 */
public final class CDTypeRelations {

  private CDTypeRelations() {}

  /** Returns the first declared superclass, preserving the CD's declaration order. */
  public static Optional<String> firstSuperclassName(ASTCDType type) {
    if (type != null && !type.getSuperclassList().isEmpty()) {
      return Optional.of(printTypeReference(type.getSuperclassList().get(0)));
    }
    return Optional.empty();
  }

  /** Returns whether the type declares at least one superclass. */
  public static boolean hasSuperclass(ASTCDType type) {
    return type != null && !type.getSuperclassList().isEmpty();
  }

  /** Returns whether the type has an explicit {@code abstract} modifier. */
  public static boolean isAbstract(ASTCDType type) {
    return type != null && type.getModifier() != null && type.getModifier().isAbstract();
  }

  /** Returns directly declared interface names in declaration order. */
  public static List<String> interfaceNames(ASTCDType type) {
    if (type == null) {
      return List.of();
    }
    return type.getInterfaceList().stream().map(CDTypeRelations::printTypeReference).toList();
  }

  /** Renders a type reference, using {@code Object} for a missing reference. */
  public static String printTypeReference(ASTMCType typeReference) {
    return typeReference != null ? JavaLoader.print(typeReference) : "Object";
  }
}
