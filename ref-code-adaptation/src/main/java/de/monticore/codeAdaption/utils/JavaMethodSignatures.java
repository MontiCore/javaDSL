package de.monticore.codeAdaption.utils;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTMCBasicGenericType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mccollectiontypes._ast.ASTMCGenericType;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Creates method lookup keys without manually splitting Java type syntax. */
public final class JavaMethodSignatures {

  private static final String PARAMETER_CONTAINER = "__MethodParameters";

  private JavaMethodSignatures() {}

  public static String normalize(String signature) {
    if (signature == null) {
      return "";
    }
    String normalized = signature.trim();
    int open = normalized.indexOf('(');
    int close = normalized.lastIndexOf(')');
    if (open < 1
        || close < open
        || close != normalized.length() - 1
        || normalized.indexOf('(', open + 1) >= 0) {
      return normalized;
    }

    String methodName = normalized.substring(0, open).trim();
    if (!isMethodName(methodName)) {
      return normalized;
    }
    String parameters = normalized.substring(open + 1, close).trim();
    if (parameters.isEmpty()) {
      return methodName + "()";
    }
    return parseParameterTypes(parameters)
        .map(types -> methodName + "(" + String.join(",", types) + ")")
        .orElse(normalized);
  }

  private static boolean isMethodName(String name) {
    try {
      return JavaDSLMill.parser().parse_StringMethodDeclaration("void " + name + "() {}").isPresent();
    } catch (IOException | RuntimeException ignored) {
      return false;
    }
  }

  private static Optional<List<String>> parseParameterTypes(String parameters) {
    try {
      Optional<ASTMCType> parsed =
          JavaDSLMill.parser().parse_StringMCType(PARAMETER_CONTAINER + "<" + parameters + ">");
      if (parsed.isEmpty()) {
        return Optional.empty();
      }
      List<ASTMCTypeArgument> arguments;
      if (parsed.get() instanceof ASTMCBasicGenericType basicGeneric) {
        arguments = basicGeneric.getMCTypeArgumentList();
      } else if (parsed.get() instanceof ASTMCGenericType generic) {
        arguments = generic.getMCTypeArgumentList();
      } else {
        return Optional.empty();
      }
      List<String> types = new ArrayList<>();
      for (ASTMCTypeArgument argument : arguments) {
        if (argument.getMCTypeOpt().isEmpty()) {
          return Optional.empty();
        }
        types.add(JavaSourceNames.printNormalizedType(argument.getMCTypeOpt().get()));
      }
      return Optional.of(List.copyOf(types));
    } catch (IOException | RuntimeException ignored) {
      return Optional.empty();
    }
  }
}
