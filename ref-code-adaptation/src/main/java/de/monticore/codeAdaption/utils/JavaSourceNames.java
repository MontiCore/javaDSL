package de.monticore.codeAdaption.utils;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.types.mcarraytypes._ast.ASTMCArrayType;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mccollectiontypes._ast.ASTMCGenericType;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import de.monticore.types.mcfullgenerictypes._ast.ASTMCWildcardTypeArgument;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import spoon.Launcher;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtWildcardReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.VirtualFile;

/** Shared Java/CD naming and type helpers used by the adapter pipeline. */
public final class JavaSourceNames {

  private JavaSourceNames() {}

  public static String simpleName(String name) {
    if (name == null) {
      return "";
    }
    String trimmed = beforeParameterList(name.trim());
    if (trimmed.isEmpty()) {
      return "";
    }
    return parseTypeKey(trimmed)
        .map(TypeKey::simpleName)
        .orElseGet(() -> fallbackSimpleName(trimmed));
  }

  public static String simpleTypeName(String typeName) {
    return simpleName(typeName);
  }

  public static String normalizeType(String type) {
    String normalized = type == null ? "" : type.trim();
    if (normalized.isBlank()) {
      return "Object";
    }
    return parseTypeKey(normalized)
        .map(TypeKey::normalized)
        .orElseGet(() -> fallbackNormalize(normalized));
  }

  public static String printNormalizedType(ASTMCType type) {
    return TypeKey.from(type).normalized();
  }

  public static String printNormalizedFieldType(ASTCDAttribute attribute) {
    return printNormalizedType(attribute.getMCType());
  }

  public static String printNormalizedReturnType(ASTCDMethod method) {
    return TypeKey.from(method.getMCReturnType()).normalized();
  }

  public static String methodSignature(ASTCDMethod method) {
    List<String> parameters = new ArrayList<>();
    for (ASTCDParameter parameter : method.getCDParameterList()) {
      parameters.add(printNormalizedType(parameter.getMCType()));
    }
    return method.getName() + "(" + String.join(",", parameters) + ")";
  }

  public static String capitalize(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    return Character.toUpperCase(value.charAt(0)) + value.substring(1);
  }

  public static String uncapitalize(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    return Character.toLowerCase(value.charAt(0)) + value.substring(1);
  }

  public static int pathDepth(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return 0;
    }
    try {
      return Path.of(fileName).getNameCount();
    } catch (InvalidPathException ignored) {
      return fileName.replace('\\', '/').split("/").length;
    }
  }

  /**
   * Rewrites simple type-name leaves inside a printed Java type while preserving parsed generic and
   * array structure. The mapper receives each leaf simple name and can return a replacement.
   */
  public static String replaceSimpleTypeNames(
      String rawType, Function<String, Optional<String>> replacementForSimpleName) {
    if (rawType == null || rawType.isEmpty()) {
      return rawType;
    }
    Optional<TypeKey> parsed = parseTypeKey(rawType.trim());
    if (parsed.isEmpty()) {
      Optional<String> replacement =
          replacementForSimpleName.apply(fallbackSimpleName(rawType.trim()));
      return replacement.orElse(rawType);
    }
    RewriteResult rewritten = parsed.get().rewrite(replacementForSimpleName);
    return rewritten.changed() ? rewritten.type().render(false, true) : rawType;
  }

  private static Optional<TypeKey> parseTypeKey(String rawType) {
    try {
      Optional<ASTMCReturnType> returnType =
          JavaDSLMill.parser().parse_StringMCReturnType(rawType);
      if (returnType.isPresent()) {
        return Optional.of(TypeKey.from(returnType.get()));
      }
    } catch (IOException | RuntimeException ignored) {
      // Fall through to the Java type parser and, if necessary, Spoon.
    }

    try {
      Optional<ASTMCType> type = JavaDSLMill.parser().parse_StringMCType(rawType);
      if (type.isPresent()) {
        return Optional.of(TypeKey.from(type.get()));
      }
    } catch (IOException | RuntimeException ignored) {
      // Fall through to Spoon, which accepts some Java fragments MontiCore rejects.
    }

    return parseTypeKeyWithSpoon(rawType);
  }

  private static Optional<TypeKey> parseTypeKeyWithSpoon(String rawType) {
    try {
      Launcher launcher = new Launcher();
      launcher.getEnvironment().setNoClasspath(true);
      launcher.addInputResource(
          new VirtualFile("class __TypeProbe { " + rawType + " value; }", "__TypeProbe.java"));
      launcher.buildModel();
      return launcher.getModel().getElements(new TypeFilter<>(CtField.class)).stream()
          .findFirst()
          .map(CtField::getType)
          .map(TypeKey::from);
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  private static String beforeParameterList(String value) {
    int paren = value.indexOf('(');
    return paren >= 0 ? value.substring(0, paren).trim() : value;
  }

  private static String fallbackNormalize(String value) {
    String compact = value.replace(" ", "");
    if ("any".equals(compact)) {
      return "Object";
    }
    return fallbackSimpleName(compact);
  }

  private static String fallbackSimpleName(String value) {
    String simple = value.trim();
    while (simple.endsWith("[]")) {
      simple = simple.substring(0, simple.length() - 2).trim();
    }
    int generic = simple.indexOf('<');
    if (generic >= 0) {
      simple = simple.substring(0, generic).trim();
    }
    int dot = simple.lastIndexOf('.');
    return dot < 0 ? simple : simple.substring(dot + 1).trim();
  }

  private record TypeKey(String name, List<TypeArgumentKey> arguments, int arrayDimensions) {

    private static TypeKey from(ASTMCReturnType returnType) {
      if (returnType.isPresentMCVoidType()) {
        return named("void");
      }
      return from(returnType.getMCType());
    }

    private static TypeKey from(ASTMCType type) {
      if (type instanceof ASTMCArrayType arrayType) {
        return from(arrayType.getMCType()).withAdditionalArrays(arrayType.getDimensions());
      }
      if (type instanceof ASTMCPrimitiveType primitiveType) {
        return named(primitiveType.printType());
      }
      if (type instanceof de.monticore.java.javadsl._ast.ASTMCBasicGenericType basicGenericType) {
        return new TypeKey(
            basicGenericType.getAnnotatedNameList().stream()
                .map(de.monticore.java.javadsl._ast.ASTAnnotatedName::getName)
                .reduce((left, right) -> left + "." + right)
                .orElse(""),
            basicGenericType.getMCTypeArgumentList().stream()
                .map(TypeArgumentKey::from)
                .toList(),
            0);
      }
      if (type instanceof ASTMCGenericType genericType) {
        return new TypeKey(
            String.join(".", genericType.getNameList()),
            genericType.getMCTypeArgumentList().stream()
                .map(TypeArgumentKey::from)
                .toList(),
            0);
      }
      if (type instanceof ASTMCQualifiedType qualifiedType) {
        return named(qualifiedType.getMCQualifiedName().getQName());
      }
      return parseTypeKeyWithSpoon(type.printType()).orElseGet(() -> named(type.printType()));
    }

    private static TypeKey from(CtTypeReference<?> reference) {
      if (reference instanceof CtArrayTypeReference<?> arrayReference) {
        return from(arrayReference.getComponentType())
            .withAdditionalArrays(arrayReference.getDimensionCount());
      }
      if (reference instanceof CtWildcardReference wildcardReference) {
        return named(wildcardReference.getSimpleName());
      }
      String qualifiedName = reference.getQualifiedName();
      String typeName =
          qualifiedName == null || qualifiedName.isBlank()
              ? reference.getSimpleName()
              : qualifiedName;
      return new TypeKey(
          typeName,
          reference.getActualTypeArguments().stream()
              .map(typeArgument -> (TypeArgumentKey) new ConcreteTypeArgument(from(typeArgument)))
              .toList(),
          0);
    }

    private static TypeKey named(String name) {
      return new TypeKey(name, List.of(), 0);
    }

    private TypeKey withAdditionalArrays(int additionalArrayDimensions) {
      return new TypeKey(name, arguments, arrayDimensions + additionalArrayDimensions);
    }

    private String simpleName() {
      return normalizeLeafName(name);
    }

    private String normalized() {
      return render(true, false);
    }

    private String render(boolean normalizeNames, boolean spaced) {
      StringBuilder result = new StringBuilder(normalizeNames ? normalizeLeafName(name) : name);
      if (!arguments.isEmpty()) {
        String delimiter = spaced ? ", " : ",";
        result.append("<");
        for (int i = 0; i < arguments.size(); i++) {
          if (i > 0) {
            result.append(delimiter);
          }
          result.append(arguments.get(i).render(normalizeNames, spaced));
        }
        result.append(">");
      }
      result.append("[]".repeat(arrayDimensions));
      return result.toString();
    }

    private RewriteResult rewrite(Function<String, Optional<String>> replacementForSimpleName) {
      boolean changed = false;
      String rewrittenName = name;
      Optional<String> replacement = replacementForSimpleName.apply(simpleName());
      if (replacement.isPresent()) {
        rewrittenName = replacement.get();
        changed = true;
      }

      List<TypeArgumentKey> rewrittenArguments = new ArrayList<>();
      for (TypeArgumentKey argument : arguments) {
        TypeArgumentRewrite rewritten = argument.rewrite(replacementForSimpleName);
        rewrittenArguments.add(rewritten.argument());
        changed |= rewritten.changed();
      }
      return new RewriteResult(
          new TypeKey(rewrittenName, List.copyOf(rewrittenArguments), arrayDimensions), changed);
    }

    private static String normalizeLeafName(String value) {
      if (value == null || value.isBlank() || "any".equals(value)) {
        return "Object";
      }
      int dot = value.lastIndexOf('.');
      String simple = dot < 0 ? value : value.substring(dot + 1);
      return "any".equals(simple) ? "Object" : simple;
    }
  }

  private interface TypeArgumentKey {
    static TypeArgumentKey from(ASTMCTypeArgument argument) {
      if (argument instanceof ASTMCWildcardTypeArgument wildcard) {
        if (wildcard.isPresentUpperBound()) {
          return new WildcardTypeArgument(Optional.of(TypeKey.from(wildcard.getUpperBound())), true);
        }
        if (wildcard.isPresentLowerBound()) {
          return new WildcardTypeArgument(
              Optional.of(TypeKey.from(wildcard.getLowerBound())), false);
        }
        return new WildcardTypeArgument(Optional.empty(), true);
      }
      return argument
          .getMCTypeOpt()
          .<TypeArgumentKey>map(type -> new ConcreteTypeArgument(TypeKey.from(type)))
          .orElseGet(() -> new UnknownTypeArgument(argument.printType()));
    }

    String render(boolean normalizeNames, boolean spaced);

    TypeArgumentRewrite rewrite(Function<String, Optional<String>> replacementForSimpleName);
  }

  private record ConcreteTypeArgument(TypeKey type) implements TypeArgumentKey {
    @Override
    public String render(boolean normalizeNames, boolean spaced) {
      return type.render(normalizeNames, spaced);
    }

    @Override
    public TypeArgumentRewrite rewrite(
        Function<String, Optional<String>> replacementForSimpleName) {
      RewriteResult rewritten = type.rewrite(replacementForSimpleName);
      return new TypeArgumentRewrite(new ConcreteTypeArgument(rewritten.type()), rewritten.changed());
    }
  }

  private record WildcardTypeArgument(Optional<TypeKey> bound, boolean upper)
      implements TypeArgumentKey {
    @Override
    public String render(boolean normalizeNames, boolean spaced) {
      if (bound.isEmpty()) {
        return "?";
      }
      String separator = spaced ? (upper ? " extends " : " super ") : (upper ? "extends" : "super");
      return "?" + separator + bound.get().render(normalizeNames, spaced);
    }

    @Override
    public TypeArgumentRewrite rewrite(
        Function<String, Optional<String>> replacementForSimpleName) {
      if (bound.isEmpty()) {
        return new TypeArgumentRewrite(this, false);
      }
      RewriteResult rewritten = bound.get().rewrite(replacementForSimpleName);
      return new TypeArgumentRewrite(
          new WildcardTypeArgument(Optional.of(rewritten.type()), upper), rewritten.changed());
    }
  }

  private record UnknownTypeArgument(String raw) implements TypeArgumentKey {
    @Override
    public String render(boolean normalizeNames, boolean spaced) {
      return normalizeNames ? normalizeType(raw) : raw;
    }

    @Override
    public TypeArgumentRewrite rewrite(
        Function<String, Optional<String>> replacementForSimpleName) {
      String rewritten = replaceSimpleTypeNames(raw, replacementForSimpleName);
      return new TypeArgumentRewrite(new UnknownTypeArgument(rewritten), !rewritten.equals(raw));
    }
  }

  private record RewriteResult(TypeKey type, boolean changed) {}

  private record TypeArgumentRewrite(TypeArgumentKey argument, boolean changed) {}
}
