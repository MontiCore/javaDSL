package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
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
  public enum Kind {
    TYPE,
    FIELD,
    METHOD
  }

  private final Kind kind;
  private final String ownerType;
  private final String name;
  private final String fieldKind;
  private final List<String> parameterTypes;
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
    this.fieldKind = normalizeType(fieldKind);
    this.parameterTypes =
        parameterTypes == null
            ? List.of()
            : Collections.unmodifiableList(parameterTypes.stream().map(StableElementKey::normalizeType).toList());
    this.returnType = normalizeType(returnType);
  }

  public static StableElementKey type(ASTCDType type) {
    return type(type.getName());
  }

  public static StableElementKey type(String typeName) {
    return new StableElementKey(Kind.TYPE, null, typeName, null, List.of(), null);
  }

  public static StableElementKey field(ASTCDType owner, ASTCDAttribute attribute) {
    return field(owner.getName(), attribute.getName(), JavaSourceNames.printNormalizedFieldType(attribute));
  }

  public static StableElementKey field(String ownerType, String fieldName, String fieldKind) {
    return new StableElementKey(Kind.FIELD, ownerType, fieldName, fieldKind, List.of(), null);
  }

  public static StableElementKey method(ASTCDType owner, ASTCDMethod method) {
    List<String> parameters = new ArrayList<>();
    for (ASTCDParameter parameter : method.getCDParameterList()) {
      parameters.add(JavaSourceNames.printNormalizedType(parameter.getMCType()));
    }
    String returnType = JavaSourceNames.printNormalizedReturnType(method);
    return method(owner.getName(), method.getName(), parameters, returnType);
  }

  public static StableElementKey method(String ownerType, String methodName, List<String> parameterTypes) {
    return method(ownerType, methodName, parameterTypes, null);
  }

  public static StableElementKey method(
      String ownerType, String methodName, List<String> parameterTypes, String returnType) {
    return new StableElementKey(Kind.METHOD, ownerType, methodName, null, parameterTypes, returnType);
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

  public List<String> getParameterTypes() {
    return parameterTypes;
  }

  public Optional<String> getReturnType() {
    return Optional.ofNullable(returnType);
  }

  public boolean sameSignatureIgnoringReturn(StableElementKey other) {
    return other != null
        && kind == other.kind
        && Objects.equals(ownerType, other.ownerType)
        && Objects.equals(name, other.name)
        && Objects.equals(parameterTypes, other.parameterTypes);
  }

  public String signature() {
    if (kind == Kind.TYPE) {
      return name;
    }
    if (kind == Kind.FIELD) {
      return ownerType + "." + name + ":" + fieldKind;
    }
    return ownerType + "." + name + "(" + String.join(",", parameterTypes) + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StableElementKey)) {
      return false;
    }
    StableElementKey other = (StableElementKey) obj;
    return kind == other.kind
        && Objects.equals(ownerType, other.ownerType)
        && Objects.equals(name, other.name)
        && Objects.equals(fieldKind, other.fieldKind)
        && Objects.equals(parameterTypes, other.parameterTypes)
        && Objects.equals(returnType, other.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, ownerType, name, fieldKind, parameterTypes, returnType);
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
}
