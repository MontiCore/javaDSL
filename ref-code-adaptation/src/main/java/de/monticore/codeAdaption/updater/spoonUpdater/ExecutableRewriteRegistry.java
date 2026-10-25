package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stores validated executable-signature and stable-key rewrite requests for one Spoon pass. */
final class ExecutableRewriteRegistry {
  private final Map<String, Set<ConcreteMethodSignature>> legacySignaturesByName =
      new LinkedHashMap<>();
  private final Map<StableElementKey, MethodRewrite> rewritesByReference = new LinkedHashMap<>();

  void clear() {
    legacySignaturesByName.clear();
    rewritesByReference.clear();
  }

  void registerConcreteSignature(String methodName, List<String> parameterTypes) {
    ConcreteMethodSignature signature =
        ConcreteMethodSignature.create(methodName, parameterTypes);
    legacySignaturesByName
        .computeIfAbsent(signature.methodName(), ignored -> new LinkedHashSet<>())
        .add(signature);
  }

  void registerRewrite(StableElementKey reference, StableElementKey concrete) {
    MethodRewrite rewrite = new MethodRewrite(reference, concrete);
    rewritesByReference.put(rewrite.reference(), rewrite);
  }

  boolean isEmpty() {
    return legacySignaturesByName.isEmpty() && rewritesByReference.isEmpty();
  }

  boolean hasRewrites() {
    return !rewritesByReference.isEmpty();
  }

  Collection<MethodRewrite> rewrites() {
    return List.copyOf(rewritesByReference.values());
  }

  List<String> unambiguousLegacyParameters(String methodName) {
    Set<ConcreteMethodSignature> signatures = legacySignaturesByName.get(methodName);
    return signatures == null || signatures.size() != 1
        ? null
        : signatures.iterator().next().parameterTypes();
  }

  long concreteTargetsNamed(String methodName) {
    return rewritesByReference.values().stream()
        .map(MethodRewrite::concrete)
        .filter(key -> methodName.equals(key.getName()))
        .count();
  }

  /** Normalized legacy method signature registered without owner identity. */
  record ConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    ConcreteMethodSignature {
      if (methodName == null || methodName.isBlank()) {
        throw new IllegalArgumentException("Method name must not be blank");
      }
      if (parameterTypes == null) {
        throw new IllegalArgumentException("Parameter types must not be null");
      }
      parameterTypes = parameterTypes.stream().map(JavaSourceNames::normalizeType).toList();
    }

    static ConcreteMethodSignature create(String methodName, List<String> parameterTypes) {
      return new ConcreteMethodSignature(methodName, parameterTypes);
    }
  }

  /** Owner- and signature-aware mapping from a reference method to its concrete target. */
  record MethodRewrite(StableElementKey reference, StableElementKey concrete) {
    MethodRewrite {
      if (reference == null
          || concrete == null
          || reference.getKind() != StableElementKey.Kind.METHOD
          || concrete.getKind() != StableElementKey.Kind.METHOD) {
        throw new IllegalArgumentException("Method rewrites require two method keys");
      }
    }
  }
}
