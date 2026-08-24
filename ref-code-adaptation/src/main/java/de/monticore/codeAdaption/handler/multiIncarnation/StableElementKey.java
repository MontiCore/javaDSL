package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stable identity for mapped CD elements. Symbols are still kept as metadata, but lookups should
 * use these keys so independently loaded ASTs can resolve the same mapped element.
 */
public final class StableElementKey {
  /** Supported CD element identity kinds. */
  public enum Kind {
    /** CD class, interface, or enum identity. */
    TYPE,
    /** Owner, name, and normalized type identity. */
    FIELD,
    /** Owner, name, normalized parameters, and optional return type identity. */
    METHOD
  }

  private final Kind kind;
  private final String ownerType;
  private final String name;
  private final String fieldKind;
  private final List<String> parameterTypes;
  private final List<String> parameterTypeIdentities;
  private final List<String> parameterTypeSources;
  private final String returnType;

  private StableElementKey(
      Kind kind,
      String ownerType,
      String name,
      String fieldKind,
      List<String> parameterTypes,
      String returnType) {
    this.kind = Objects.requireNonNull(kind);
    this.ownerType = normalize(ownerType);
    this.name = normalize(name);
    this.fieldKind = normalizeTypeIdentity(fieldKind);
    this.parameterTypes =
        parameterTypes == null
            ? List.of()
            : Collections.unmodifiableList(
                parameterTypes.stream().map(StableElementKey::normalizeType).toList());
    this.parameterTypeIdentities =
        parameterTypes == null
            ? List.of()
            : Collections.unmodifiableList(
                parameterTypes.stream().map(StableElementKey::normalizeParameterIdentity).toList());
    this.parameterTypeSources =
        parameterTypes == null
            ? List.of()
            : Collections.unmodifiableList(
                parameterTypes.stream().map(StableElementKey::normalizeTypeSource).toList());
    this.returnType = normalizeTypeIdentity(returnType);
  }

  /** Creates a stable key for a CD type AST. */
  public static StableElementKey type(ASTCDType type) {
    return type(type.getName());
  }

  /** Creates a stable key for a type name. */
  public static StableElementKey type(String typeName) {
    return new StableElementKey(Kind.TYPE, null, typeName, null, List.of(), null);
  }

  /** Creates an owner-aware stable key for a CD attribute AST. */
  public static StableElementKey field(ASTCDType owner, ASTCDAttribute attribute) {
    return field(
        owner.getName(),
        attribute.getName(),
        JavaSourceNames.printQualifiedType(attribute.getMCType()));
  }

  /** Creates an owner-aware field key from normalized identity components. */
  public static StableElementKey field(String ownerType, String fieldName, String fieldKind) {
    return new StableElementKey(Kind.FIELD, ownerType, fieldName, fieldKind, List.of(), null);
  }

  /** Creates an owner- and signature-aware stable key for a CD method AST. */
  public static StableElementKey method(ASTCDType owner, ASTCDMethod method) {
    List<String> parameters = new ArrayList<>();
    for (ASTCDParameter parameter : method.getCDParameterList()) {
      parameters.add(JavaSourceNames.printQualifiedType(parameter.getMCType()));
    }
    String returnType = JavaSourceNames.printQualifiedReturnType(method);
    return method(owner.getName(), method.getName(), parameters, returnType);
  }

  /** Creates a method key without return-type identity. */
  public static StableElementKey method(String ownerType, String methodName, List<String> parameterTypes) {
    return method(ownerType, methodName, parameterTypes, null);
  }

  /** Creates a method key with optional return-type identity. */
  public static StableElementKey method(
      String ownerType, String methodName, List<String> parameterTypes, String returnType) {
    return new StableElementKey(Kind.METHOD, ownerType, methodName, null, parameterTypes, returnType);
  }

  /** Builds a stable key for a symbol using the owning CD index for member ownership. */
  public static Optional<StableElementKey> fromSymbol(ISymbol symbol, CDModelIndex index) {
    if (symbol == null || index == null) {
      return Optional.empty();
    }
    if (symbol.getAstNode() instanceof ASTCDType type) {
      return Optional.of(type(type));
    }
    if (symbol.getAstNode() instanceof ASTCDAttribute attribute) {
      return index.ownerOf(attribute).map(owner -> field(owner, attribute));
    }
    if (symbol.getAstNode() instanceof ASTCDMethod method) {
      return index.ownerOf(method).map(owner -> method(owner, method));
    }
    return Optional.empty();
  }

  public Kind getKind() {
    return kind;
  }

  public Optional<String> getOwnerType() {
    return Optional.ofNullable(ownerType);
  }

  public String getName() {
    return name;
  }

  public Optional<String> getFieldKind() {
    return Optional.ofNullable(fieldKind);
  }

  /**
   * Returns normalized simple-name parameter types for Java mutation and lookup consumers.
   * Equality and hashing use a separate qualification-preserving identity.
   */
  public List<String> getParameterTypes() {
    return parameterTypes;
  }

  /**
   * Returns the supplied parameter spellings with qualification intact. Stable-key equality uses a
   * normalized form of these source identities so overloads such as {@code alpha.Role} and {@code
   * beta.Role} remain distinct.
   */
  public List<String> getParameterTypeSources() {
    return parameterTypeSources;
  }

  public Optional<String> getReturnType() {
    return Optional.ofNullable(returnType);
  }

  /** Returns the same member identity anchored to a resolved Java declaring type. */
  public StableElementKey withOwnerType(String resolvedOwnerType) {
    if (kind == Kind.TYPE) {
      throw new IllegalStateException("A type key has no declaring owner");
    }
    return new StableElementKey(
        kind,
        resolvedOwnerType,
        name,
        fieldKind,
        parameterTypeSources,
        returnType);
  }

  /** Returns whether both keys identify the same element without considering method return type. */
  public boolean sameSignatureIgnoringReturn(StableElementKey other) {
    return other != null
        && kind == other.kind
        && Objects.equals(ownerType, other.ownerType)
        && Objects.equals(name, other.name)
        && Objects.equals(parameterTypeIdentities, other.parameterTypeIdentities);
  }

  /** Returns the deterministic owner/name/type signature used in diagnostics and sorting. */
  public String signature() {
    if (kind == Kind.TYPE) {
      return name;
    }
    if (kind == Kind.FIELD) {
      return ownerType + "." + name + ":" + fieldKind;
    }
    return ownerType + "." + name + "(" + String.join(",", parameterTypeIdentities) + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StableElementKey other)) {
      return false;
    }
      return kind == other.kind
        && Objects.equals(ownerType, other.ownerType)
        && Objects.equals(name, other.name)
        && Objects.equals(fieldKind, other.fieldKind)
        && Objects.equals(parameterTypeIdentities, other.parameterTypeIdentities)
        && Objects.equals(returnType, other.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, ownerType, name, fieldKind, parameterTypeIdentities, returnType);
  }

  @Override
  public String toString() {
    return signature() + (returnType == null ? "" : ":" + returnType);
  }

  private static String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static String normalizeType(String value) {
    String normalized = normalize(value);
    if (normalized == null) {
      return null;
    }
    return JavaSourceNames.normalizeType(normalized);
  }

  private static String normalizeTypeSource(String value) {
    String normalized = normalize(value);
    return normalized == null ? "Object" : normalized;
  }

  private static String normalizeParameterIdentity(String value) {
    return JavaSourceNames.canonicalType(normalizeTypeSource(value));
  }

  private static String normalizeTypeIdentity(String value) {
    String normalized = normalize(value);
    return normalized == null ? null : JavaSourceNames.canonicalType(normalized);
  }
}
