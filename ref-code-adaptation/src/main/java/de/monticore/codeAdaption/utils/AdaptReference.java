package de.monticore.codeAdaption.utils;

import de.monticore.java.javadsl.JavaDSLMill;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * Parsed reference to a type, field, or method used by {@code @Adapt} mappings.
 *
 * <p>References may be local, such as {@code name} and {@code update(String)}, or owner-qualified,
 * such as {@code model.Order.name} and {@code model.Order.update(java.time.Instant)}. The owner is
 * separated only before the method's opening parenthesis, while JavaDSL validates owner and method
 * syntax and normalizes parameter types through {@link JavaMethodSignatures}.
 *
 * <p>The small amount of delimiter handling in this class is intentional: an adapter method
 * reference contains parameter types without parameter names, so it is neither a Java invocation
 * nor a Java declaration that JavaDSL could parse as a whole. All nested Java type syntax is still
 * delegated to JavaDSL.
 *
 * @param owner optional qualified owner preceding the referenced member
 * @param memberName unqualified type or member name
 * @param methodSignature normalized method signature when the reference denotes a method
 */
public record AdaptReference(
    Optional<String> owner, String memberName, Optional<String> methodSignature) {

  public AdaptReference {
    owner = Objects.requireNonNull(owner, "owner");
    memberName = Objects.requireNonNull(memberName, "memberName");
    methodSignature = Objects.requireNonNull(methodSignature, "methodSignature");
  }

  /**
   * Parses one adapter reference without confusing dots in qualified parameter types with the
   * owner/member separator.
   *
   * @param value textual adapter reference
   * @return the parsed reference, or empty when its owner, member, or signature is malformed
   */
  public static Optional<AdaptReference> parse(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    String reference = value.trim();
    int open = reference.indexOf('(');
    int memberEnd = open < 0 ? reference.length() : open;
    int separator = reference.lastIndexOf('.', memberEnd - 1);
    String owner = separator < 0 ? "" : reference.substring(0, separator).trim();
    String memberReference = reference.substring(separator + 1).trim();

    if ((separator >= 0 && owner.isEmpty()) || memberReference.isEmpty() || !isOwner(owner)) {
      return Optional.empty();
    }
    if (open >= 0 || reference.indexOf(')') >= 0) {
      Optional<String> signature = JavaMethodSignatures.parseNormalized(memberReference);
      if (signature.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(
          new AdaptReference(
              optional(owner), JavaSourceNames.simpleName(signature.get()), signature));
    }
    if (JavaMethodSignatures.parseNormalized(memberReference + "()").isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new AdaptReference(optional(owner), memberReference, Optional.empty()));
  }

  /** Returns the normalized member reference, including parameters for methods. */
  public String memberReference() {
    return methodSignature.orElse(memberName);
  }

  /** Returns whether this reference explicitly denotes a method. */
  public boolean isMethod() {
    return methodSignature.isPresent();
  }

  private static Optional<String> optional(String value) {
    return value.isEmpty() ? Optional.empty() : Optional.of(value);
  }

  private static boolean isOwner(String owner) {
    if (owner.isEmpty()) {
      return true;
    }
    try {
      return JavaDSLMill.parser().parse_StringMCType(owner).isPresent();
    } catch (IOException | RuntimeException ignored) {
      return false;
    }
  }
}
