package de.monticore.codeAdaption.utils;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTMCBasicGenericType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Creates method lookup keys by parsing Java type syntax instead of manually splitting nested
 * generic parameter lists.
 */
public final class JavaMethodSignatures {

  private static final String PARAMETER_CONTAINER = "__MethodParameters";

  private JavaMethodSignatures() {}

  /**
   * Normalizes a method name and its parameter types for owner-aware lookup.
   *
   * <p>Malformed or unsupported signatures are returned trimmed but otherwise unchanged so an
   * invalid key cannot accidentally match a different method.
   *
   * @param signature textual method signature
   * @return normalized lookup key, an empty string for {@code null}, or unchanged malformed input
   */
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

  /**
   * Parses a comma-separated parameter list by embedding it as the type arguments of a synthetic
   * generic type. This lets the JavaDSL grammar, rather than string splitting, distinguish commas
   * between parameters from commas inside nested generic types.
   *
   * <p>The arbitrary container name is parsed by JavaDSL as {@link ASTMCBasicGenericType}; its type
   * arguments are the original method parameter types.
   */
  private static Optional<List<String>> parseParameterTypes(String parameters) {
    try {
      Optional<ASTMCType> parsed =
          JavaDSLMill.parser().parse_StringMCType(PARAMETER_CONTAINER + "<" + parameters + ">");
      if (parsed.isEmpty()) {
        return Optional.empty();
      }
      if (!(parsed.get() instanceof ASTMCBasicGenericType basicGeneric)) {
        return Optional.empty();
      }
      List<ASTMCTypeArgument> arguments = basicGeneric.getMCTypeArgumentList();
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
