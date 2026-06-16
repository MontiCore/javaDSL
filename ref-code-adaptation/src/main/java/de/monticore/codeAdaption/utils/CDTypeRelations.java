package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Centralizes access to CD type relationships that differ across generated AST APIs. */
public final class CDTypeRelations {

  private CDTypeRelations() {}

  // TODO
  public static Optional<String> firstSuperclassName(ASTCDType type) {
    if (type instanceof ASTCDClass cdClass && !cdClass.getSuperclassList().isEmpty()) {
      return Optional.of(printTypeReference(cdClass.getSuperclassList().get(0)));
    }
    return Optional.empty();
  }

  public static boolean hasSuperclass(ASTCDType type) {
    return type instanceof ASTCDClass cdClass && !cdClass.getSuperclassList().isEmpty();
  }

  public static boolean isAbstract(ASTCDType type) {
    try {
      Object modifier = type.getClass().getMethod("getModifier").invoke(type);
      Object result = modifier.getClass().getMethod("isAbstract").invoke(modifier);
      return Boolean.TRUE.equals(result);
    } catch (ReflectiveOperationException ignored) {
      return false;
    }
  }

  public static List<String> interfaceNames(ASTCDType type) {
    List<?> interfaces = readList(type, "getInterfaceList");
    if (interfaces.isEmpty()) {
      interfaces = readList(type, "getInterfaces");
    }
    List<String> names = new ArrayList<>();
    for (Object element : interfaces) {
      if (element != null) {
        names.add(printTypeReference(element));
      }
    }
    return names;
  }

  public static String printTypeReference(Object typeReference) {
    if (typeReference instanceof ASTMCType mcType) {
      return JavaLoader.print(mcType);
    }
    Optional<Object> qualifiedType = invoke(typeReference, "getMCQualifiedType");
    if (qualifiedType.orElse(null) instanceof ASTMCType mcType) {
      return JavaLoader.print(mcType);
    }
    Optional<Object> printed = invoke(typeReference, "printType");
    if (printed.isPresent()) {
      return printed.get().toString();
    }
    Optional<Object> name = invoke(typeReference, "getName");
    if (name.isPresent()) {
      return name.get().toString();
    }
    return "Object";
  }

  private static List<?> readList(Object target, String methodName) {
    Optional<Object> result = invoke(target, methodName);
    if (result.orElse(null) instanceof List<?> list) {
      return list;
    }
    return List.of();
  }

  private static Optional<Object> invoke(Object target, String methodName) {
    if (target == null) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(target.getClass().getMethod(methodName).invoke(target));
    } catch (ReflectiveOperationException ignored) {
      return Optional.empty();
    }
  }
}
