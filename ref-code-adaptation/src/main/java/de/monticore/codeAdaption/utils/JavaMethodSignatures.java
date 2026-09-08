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
   * Parses and normalizes a complete method signature.
   *
   * <p>For example, {@code
   * update(java.util.Map<java.lang.String,java.util.List<any[]>>,int[],long)} becomes {@code
   * update(Map<String,List<Object[]>>,int[],long)}.
   *
   * @param signature method name followed by a parameter-type list
   * @return the normalized signature, or empty when the input is not a complete valid signature
   */
  public static Optional<String> parseNormalized(String signature) {
    if (signature == null) {
      return Optional.empty();
    }
    String normalized = signature.trim();
    int open = normalized.indexOf('(');
    int close = normalized.lastIndexOf(')');
    if (open < 1
        || close < open
        || close != normalized.length() - 1
        || normalized.indexOf('(', open + 1) >= 0) {
      return Optional.empty();
    }

    String methodName = normalized.substring(0, open).trim();
    if (!isMethodName(methodName)) {
      return Optional.empty();
    }
    String parameters = normalized.substring(open + 1, close).trim();
    if (parameters.isEmpty()) {
      return Optional.of(methodName + "()");
    }
    return parseParameterTypes(parameters)
        .map(types -> methodName + "(" + String.join(",", types) + ")");
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
