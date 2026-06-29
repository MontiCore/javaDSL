package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.List;
import java.util.Optional;

/** Centralizes access to CD type relationships that differ across generated AST APIs. */
public final class CDTypeRelations {

  private CDTypeRelations() {}

  public static Optional<String> firstSuperclassName(ASTCDType type) {
    if (type != null && !type.getSuperclassList().isEmpty()) {
      return Optional.of(printTypeReference(type.getSuperclassList().get(0)));
    }
    return Optional.empty();
  }

  public static boolean hasSuperclass(ASTCDType type) {
    return type != null && !type.getSuperclassList().isEmpty();
  }

  public static boolean isAbstract(ASTCDType type) {
    return type != null && type.getModifier() != null && type.getModifier().isAbstract();
  }

  public static List<String> interfaceNames(ASTCDType type) {
    if (type == null) {
      return List.of();
    }
    return type.getInterfaceList().stream().map(CDTypeRelations::printTypeReference).toList();
  }

  public static String printTypeReference(ASTMCType typeReference) {
    return typeReference != null ? JavaLoader.print(typeReference) : "Object";
  }
}
