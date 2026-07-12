package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/** Repairs executable references and signatures after declaration and type transformations. */
final class SpoonExecutableRepairService {
  private final SpoonWorkspace workspace;
  private final SpoonElementResolver resolver;
  private final Map<String, Set<List<String>>> legacyMethodSignatures = new LinkedHashMap<>();
  private final Map<StableElementKey, StableElementKey> methodRewrites = new LinkedHashMap<>();

  SpoonExecutableRepairService(SpoonWorkspace workspace, SpoonElementResolver resolver) {
    this.workspace = workspace;
    this.resolver = resolver;
  }

  void reset() {
    legacyMethodSignatures.clear();
    methodRewrites.clear();
  }

  void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    if (methodName != null && !methodName.isBlank() && parameterTypes != null) {
      legacyMethodSignatures
          .computeIfAbsent(methodName, ignored -> new LinkedHashSet<>())
          .add(normalizedTypes(parameterTypes));
    }
  }

  void registerMethodRewrite(StableElementKey reference, StableElementKey concrete) {
    if (reference == null
        || concrete == null
        || reference.getKind() != StableElementKey.Kind.METHOD
        || concrete.getKind() != StableElementKey.Kind.METHOD) {
      throw new IllegalArgumentException("Method rewrites require two method keys");
    }
    methodRewrites.put(reference, concrete);
  }

  MethodRename planMethodRename(
      ASTTypeDeclaration sourceType, ASTMethodDeclaration sourceMethod) {
    CtMethod<?> target = resolver.getSpoonMethod(sourceType, sourceMethod);
    String oldName = target.getSimpleName();
    CtType<?> owner = target.getDeclaringType();
    List<CtInvocation<?>> invocations = new ArrayList<>();
    for (CtInvocation<?> invocation :
        workspace.model().getElements(new TypeFilter<>(CtInvocation.class))) {
      if (invocation.getExecutable() != null
          && oldName.equals(invocation.getExecutable().getSimpleName())
          && invokesMethodOn(invocation, target, owner)) {
        invocations.add(invocation);
      }
    }
    return new MethodRename(target, List.copyOf(invocations));
  }

  void renameInvocations(MethodRename rename, String newName) {
    rename.invocations().forEach(invocation -> invocation.getExecutable().setSimpleName(newName));
  }

  void prepareForPrint(Map<String, String> groupingMappings) {
    applyConcreteMethodSignatures(groupingMappings);
    enforceInterfaceMethodBodies();
  }

  private boolean invokesMethodOn(
      CtInvocation<?> invocation, CtMethod<?> target, CtType<?> owner) {
    try {
      var declaration = invocation.getExecutable().getDeclaration();
      if (declaration != null) {
        return declaration == target;
      }
    } catch (RuntimeException ignored) {
      // Continue with type identity in no-classpath mode.
    }
    try {
      CtTypeReference<?> declaringType = invocation.getExecutable().getDeclaringType();
      if (declaringType != null && owner != null) {
        return sameTypeName(declaringType.getQualifiedName(), owner.getQualifiedName());
      }
    } catch (RuntimeException ignored) {
      // Unresolved inherited calls are handled by the lexical owner fallback below.
    }
    CtType<?> lexicalOwner = invocation.getParent(CtType.class);
    if (lexicalOwner == null || owner == null) {
      return false;
    }
    if (sameTypeName(lexicalOwner.getQualifiedName(), owner.getQualifiedName())) {
      return true;
    }
    CtTypeReference<?> superclass = lexicalOwner.getSuperclass();
    return superclass != null
        && sameTypeName(superclass.getQualifiedName(), owner.getQualifiedName());
  }

  private void applyConcreteMethodSignatures(Map<String, String> groupingMappings) {
    if (legacyMethodSignatures.isEmpty() && methodRewrites.isEmpty()) {
      return;
    }
    for (CtInvocation<?> invocation :
        workspace.model().getElements(new TypeFilter<>(CtInvocation.class))) {
      String methodName =
          invocation.getExecutable() == null ? null : invocation.getExecutable().getSimpleName();
      StableElementKey target =
          findConcreteRewriteTarget(invocation, methodName, groupingMappings);
      List<String> parameterTypes =
          target == null ? unambiguousLegacyParameters(methodName) : target.getParameterTypes();
      if (parameterTypes == null) {
        continue;
      }
      if (target != null && invocation.getExecutable() != null) {
        invocation.getExecutable().setSimpleName(target.getName());
      }
      if (invocation.getArguments().size() > parameterTypes.size()) {
        throw new IllegalStateException(
            "Concrete method '" + methodName + "' has fewer parameters than " + invocation);
      }
      for (int index = invocation.getArguments().size(); index < parameterTypes.size(); index++) {
        invocation.addArgument(missingArgument(invocation, parameterTypes.get(index)));
      }
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private CtExpression<?> missingArgument(CtInvocation<?> invocation, String parameterType) {
    CtMethod<?> enclosingMethod = invocation.getParent(CtMethod.class);
    if (enclosingMethod != null) {
      List<CtParameter<?>> compatible =
          enclosingMethod.getParameters().stream()
              .filter(parameter -> parameter.getType() != null)
              .filter(
                  parameter ->
                      sameTypeName(parameter.getType().getQualifiedName(), parameterType))
              .toList();
      if (compatible.size() == 1) {
        return workspace.factory().Code().createVariableRead(compatible.get(0).getReference(), false);
      }
    }
    return workspace.defaultExpression(parameterType);
  }

  private StableElementKey findConcreteRewriteTarget(
      CtInvocation<?> invocation,
      String methodName,
      Map<String, String> groupingMappings) {
    if (methodName == null || methodRewrites.isEmpty()) {
      return null;
    }
    String owner = ownerName(invocation);
    List<String> invocationTypes = invocationArgumentTypes(invocation);
    List<StableElementKey> matches = new ArrayList<>();
    for (Map.Entry<StableElementKey, StableElementKey> rewrite : methodRewrites.entrySet()) {
      StableElementKey reference = rewrite.getKey();
      StableElementKey concrete = rewrite.getValue();
      if ((!methodName.equals(reference.getName()) && !methodName.equals(concrete.getName()))
          || !ownerMatches(owner, reference, concrete, groupingMappings)) {
        continue;
      }
      if (!signatureMatches(reference.getParameterTypes(), invocationTypes)) {
        continue;
      }
      matches.add(concrete);
    }
    if (matches.size() == 1) {
      return matches.get(0);
    }
    if (matches.size() > 1) {
      throw new IllegalStateException(
          "Ambiguous method rewrite for '"
              + methodName
              + "' on owner '"
              + owner
              + "' with arguments "
              + invocationTypes);
    }
    return null;
  }

  private static boolean ownerMatches(
      String owner,
      StableElementKey reference,
      StableElementKey concrete,
      Map<String, String> groupingMappings) {
    if (owner == null
        || (reference.getOwnerType().isEmpty() && concrete.getOwnerType().isEmpty())) {
      return true;
    }
    String referenceOwner = reference.getOwnerType().orElse(null);
    String concreteOwner = concrete.getOwnerType().orElse(null);
    String groupedOwner = groupingMappings.get(concreteOwner);
    return sameTypeName(owner, referenceOwner)
        || sameTypeName(owner, concreteOwner)
        || sameTypeName(owner, groupedOwner);
  }

  private static boolean signatureMatches(List<String> expected, List<String> actual) {
    if (expected.size() != actual.size()) {
      return false;
    }
    for (int index = 0; index < expected.size(); index++) {
      String actualType = actual.get(index);
      if (actualType != null
          && !sameTypeName(JavaSourceNames.normalizeType(expected.get(index)), actualType)) {
        return false;
      }
    }
    return true;
  }

  private static List<String> invocationArgumentTypes(CtInvocation<?> invocation) {
    List<String> result = new ArrayList<>();
    for (CtExpression<?> argument : invocation.getArguments()) {
      CtTypeReference<?> type = argument.getType();
      String qualifiedName = type == null ? null : type.getQualifiedName();
      // Spoon represents null literals as the synthetic type "<nulltype>".
      result.add(
          qualifiedName == null || qualifiedName.isBlank() || qualifiedName.startsWith("<")
              ? null
              : JavaSourceNames.normalizeType(qualifiedName));
    }
    return result;
  }

  private List<String> unambiguousLegacyParameters(String methodName) {
    Set<List<String>> signatures = legacyMethodSignatures.get(methodName);
    if (signatures == null || signatures.size() != 1) {
      return null;
    }
    long count =
        methodRewrites.values().stream().filter(key -> methodName.equals(key.getName())).count();
    return count <= 1 ? signatures.iterator().next() : null;
  }

  private static String ownerName(CtInvocation<?> invocation) {
    try {
      if (invocation.getExecutable() != null
          && invocation.getExecutable().getDeclaringType() != null) {
        return invocation.getExecutable().getDeclaringType().getQualifiedName();
      }
    } catch (RuntimeException ignored) {
      // Spoon may not resolve declaring types in no-classpath mode.
    }
    try {
      if (invocation.getTarget() != null && invocation.getTarget().getType() != null) {
        return invocation.getTarget().getType().getQualifiedName();
      }
    } catch (RuntimeException ignored) {
      // Fall through to the lexical owner.
    }
    CtType<?> parent = invocation.getParent(CtType.class);
    return parent == null ? null : parent.getQualifiedName();
  }

  private void enforceInterfaceMethodBodies() {
    for (CtType<?> type : workspace.model().getAllTypes()) {
      if (!type.isInterface()) {
        continue;
      }
      for (CtMethod<?> method : type.getMethods()) {
        if (!method.isDefaultMethod()
            && !method.hasModifier(ModifierKind.STATIC)
            && !method.hasModifier(ModifierKind.PRIVATE)) {
          method.setBody(null);
        }
      }
    }
  }

  private static List<String> normalizedTypes(List<String> types) {
    return types.stream().map(JavaSourceNames::normalizeType).collect(Collectors.toList());
  }

  private static boolean sameTypeName(String first, String second) {
    if (first == null || second == null) {
      return false;
    }
    return first.equals(second)
        || JavaSourceNames.simpleName(first).equals(JavaSourceNames.simpleName(second));
  }

  record MethodRename(CtMethod<?> target, List<CtInvocation<?>> invocations) {}
}
