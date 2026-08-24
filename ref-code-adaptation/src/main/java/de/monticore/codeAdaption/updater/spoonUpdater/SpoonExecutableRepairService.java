package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Repairs executable references and signatures after declaration and type transformations.
 *
 * <p>This service runs after grouping and declaration rewrites. It resolves overloads by stable
 * owner/signature keys and uses guarded no-classpath fallbacks where Spoon cannot resolve a
 * declaration.
 */
final class SpoonExecutableRepairService {
  private final SpoonWorkspace workspace;
  private final SpoonElementResolver resolver;
  private final ExecutableRewriteRegistry rewriteRegistry = new ExecutableRewriteRegistry();

  /** Creates an executable repair phase over the currently loaded Spoon workspace. */
  SpoonExecutableRepairService(SpoonWorkspace workspace, SpoonElementResolver resolver) {
    this.workspace = workspace;
    this.resolver = resolver;
  }

  /** Clears all method signatures and stable-key rewrites registered for the previous pass. */
  void reset() {
    rewriteRegistry.clear();
  }

  /**
   * Registers an owner-independent concrete signature used by legacy update calls. Duplicate
   * normalized signatures are collapsed by the registry.
   */
  void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    if (methodName != null && !methodName.isBlank() && parameterTypes != null) {
      rewriteRegistry.registerConcreteSignature(methodName, parameterTypes);
    }
  }

  /** Registers an owner- and signature-aware reference-method to concrete-method rewrite. */
  void registerMethodRewrite(StableElementKey reference, StableElementKey concrete) {
    rewriteRegistry.registerRewrite(
        qualifyUniqueModelOwner(reference), qualifyUniqueModelOwner(concrete));
  }

  private StableElementKey qualifyUniqueModelOwner(StableElementKey key) {
    String owner = key.getOwnerType().orElse(null);
    if (owner == null || owner.contains(".")) {
      return key;
    }
    List<String> matches =
        workspace.model().getAllTypes().stream()
            .filter(type -> owner.equals(type.getSimpleName()))
            .map(CtType::getQualifiedName)
            .distinct()
            .toList();
    return matches.size() == 1 ? key.withOwnerType(matches.get(0)) : key;
  }

  /**
   * Resolves a source method to its Spoon declaration and captures only the invocations that target
   * that declaration. Capturing happens before the declaration is renamed so unresolved invocation
   * names can be updated afterward without affecting unrelated overloads.
   */
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

  /** Applies a planned new name to the captured invocation executable references. */
  void renameInvocations(MethodRename rename, String newName) {
    rename.invocations().forEach(invocation -> invocation.getExecutable().setSimpleName(newName));
  }

  /**
   * Performs the final executable repair phase before printing: concrete invocation signatures are
   * applied first, then illegal bodies are removed from abstract interface methods.
   */
  void prepareForPrint(Map<String, String> groupingMappings) {
    applyConcreteMethodSignatures(groupingMappings);
    enforceInterfaceMethodBodies();
  }

  /**
   * Tests whether an invocation targets a selected method using, in order, resolved declaration
   * identity, executable declaring-type identity, and a lexical owner/superclass fallback for
   * Spoon's no-classpath mode.
   */
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
        return sameOwnerTypeName(declaringType.getQualifiedName(), owner.getQualifiedName());
      }
    } catch (RuntimeException ignored) {
      // Unresolved inherited calls are handled by the lexical owner fallback below.
    }
    CtType<?> lexicalOwner = invocation.getParent(CtType.class);
    if (lexicalOwner == null || owner == null) {
      return false;
    }
    if (sameOwnerTypeName(lexicalOwner.getQualifiedName(), owner.getQualifiedName())) {
      return true;
    }
    CtTypeReference<?> superclass = lexicalOwner.getSuperclass();
    return superclass != null
        && sameOwnerTypeName(superclass.getQualifiedName(), owner.getQualifiedName());
  }

  /**
   * Reconciles every invocation with its registered concrete method signature. It renames resolved
   * targets and adds missing arguments, but rejects calls that already supply too many arguments.
   */
  private void applyConcreteMethodSignatures(Map<String, String> groupingMappings) {
    if (rewriteRegistry.isEmpty()) {
      return;
    }
    for (CtInvocation<?> invocation :
        workspace.model().getElements(new TypeFilter<>(CtInvocation.class))) {
      String methodName =
          invocation.getExecutable() == null ? null : invocation.getExecutable().getSimpleName();
      StableElementKey target =
          findConcreteRewriteTarget(invocation, methodName, groupingMappings);
      List<String> parameterTypes =
          target == null
              ? unambiguousLegacyParameters(methodName)
              : target.getParameterTypeSources();
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

  /**
   * Supplies a missing concrete argument from the unique compatible enclosing parameter, or uses
   * the Java default value when no unique parameter is available.
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private CtExpression<?> missingArgument(CtInvocation<?> invocation, String parameterType) {
    CtMethod<?> enclosingMethod = invocation.getParent(CtMethod.class);
    if (enclosingMethod != null) {
      CtTypeReference<?> expected = expectedTypeReference(enclosingMethod, parameterType);
      List<CtParameter<?>> exact =
          enclosingMethod.getParameters().stream()
              .filter(parameter -> parameter.getType() != null)
              .filter(
                  parameter ->
                      sameArgumentType(parameter.getType(), expected, parameterType))
              .toList();
      if (exact.size() == 1) {
        return workspace.factory().Code().createVariableRead(exact.get(0).getReference(), false);
      }
      if (exact.isEmpty()) {
        List<CtParameter<?>> compatibleSubtypes =
            enclosingMethod.getParameters().stream()
                .filter(parameter -> isSubtype(parameter.getType(), expected))
                .toList();
        if (compatibleSubtypes.size() == 1) {
          return workspace
              .factory()
              .Code()
              .createVariableRead(compatibleSubtypes.get(0).getReference(), false);
        }
      }
    }
    return workspace.defaultExpression(parameterType);
  }

  private CtTypeReference<?> expectedTypeReference(
      CtMethod<?> enclosingMethod, String parameterType) {
    if (parameterType != null && parameterType.contains(".")) {
      Optional<CtType<?>> qualifiedMatch =
          workspace.model().getAllTypes().stream()
              .filter(type -> parameterType.equals(type.getQualifiedName()))
              .findFirst();
      if (qualifiedMatch.isPresent()) {
        return qualifiedMatch.get().getReference();
      }
    }
    if (parameterType != null && !parameterType.contains(".")) {
      String simpleName = JavaSourceNames.simpleName(parameterType);
      List<CtType<?>> modelMatches =
          workspace.model().getAllTypes().stream()
              .filter(type -> simpleName.equals(type.getSimpleName()))
              .toList();
      if (modelMatches.size() == 1) {
        return modelMatches.get(0).getReference();
      }
      CtType<?> owner = enclosingMethod.getParent(CtType.class);
      if (owner != null && owner.getPackage() != null) {
        String localName = owner.getPackage().getQualifiedName() + "." + simpleName;
        Optional<CtType<?>> localMatch =
            modelMatches.stream()
                .filter(type -> localName.equals(type.getQualifiedName()))
                .findFirst();
        if (localMatch.isPresent()) {
          return localMatch.get().getReference();
        }
      }
    }
    return workspace.createTypeReference(parameterType);
  }

  private boolean isSubtype(CtTypeReference<?> actual, CtTypeReference<?> expected) {
    if (actual == null || expected == null || actual.isPrimitive() || expected.isPrimitive()) {
      return false;
    }
    String actualName = resolvedQualifiedName(actual);
    String expectedName = resolvedQualifiedName(expected);
    if (expectedName.contains(".") && !actualName.contains(".")) {
      return false;
    }
    if (actualName.contains(".")
        && expectedName.contains(".")
        && !actualName.equals(expectedName)
        && actual.getSimpleName().equals(expected.getSimpleName())) {
      return false;
    }
    try {
      return actual.isSubtypeOf(expected);
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  /** Does not equate distinct known qualified types that merely share a simple name. */
  private boolean sameArgumentType(
      CtTypeReference<?> actual, CtTypeReference<?> expected, String expectedSource) {
    if (actual == null || expected == null) {
      return false;
    }
    String actualIdentity = canonicalWorkspaceType(actual.toString());
    String expectedIdentity =
        canonicalWorkspaceType(expectedSource == null ? expected.toString() : expectedSource);
    return actualIdentity.equals(expectedIdentity);
  }

  private static String resolvedQualifiedName(CtTypeReference<?> type) {
    try {
      CtType<?> declaration = type.getTypeDeclaration();
      if (declaration != null && declaration.getQualifiedName() != null) {
        return declaration.getQualifiedName();
      }
    } catch (RuntimeException ignored) {
      // Fall back to the reference's own no-classpath name.
    }
    String qualifiedName = type.getQualifiedName();
    return qualifiedName == null ? type.getSimpleName() : qualifiedName;
  }

  /**
   * Selects the unique stable-key rewrite matching invocation name, owner, and argument types.
   * Null-literal argument types act as wildcards because Spoon cannot infer their target type.
   */
  private StableElementKey findConcreteRewriteTarget(
      CtInvocation<?> invocation,
      String methodName,
      Map<String, String> groupingMappings) {
    if (methodName == null || !rewriteRegistry.hasRewrites()) {
      return null;
    }
    String owner = ownerName(invocation);
    List<String> invocationTypes = invocationArgumentTypes(invocation);
    List<StableElementKey> matches = new ArrayList<>();
    for (ExecutableRewriteRegistry.MethodRewrite rewrite : rewriteRegistry.rewrites()) {
      StableElementKey reference = rewrite.reference();
      StableElementKey concrete = rewrite.concrete();
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
      StableElementKey first = matches.get(0);
      if (matches.stream().allMatch(first::sameSignatureIgnoringReturn)) {
        return first;
      }
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

  /** Returns whether an invocation owner matches the reference, concrete, or grouping owner. */
  private boolean ownerMatches(
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
    if (groupedOwner == null && concreteOwner != null) {
      groupedOwner = groupingMappings.get(JavaSourceNames.simpleName(concreteOwner));
    }
    if (groupedOwner != null
        && !groupedOwner.contains(".")
        && concreteOwner != null
        && concreteOwner.contains(".")) {
      groupedOwner =
          concreteOwner.substring(0, concreteOwner.lastIndexOf('.') + 1) + groupedOwner;
    }
    return sameOwnerTypeName(owner, referenceOwner)
        || sameOwnerTypeName(owner, concreteOwner)
        || sameOwnerTypeName(owner, groupedOwner);
  }

  /** Compares normalized parameter types, treating unknown actual argument types as compatible. */
  private boolean signatureMatches(List<String> expected, List<String> actual) {
    if (expected.size() != actual.size()) {
      return false;
    }
    for (int index = 0; index < expected.size(); index++) {
      String actualType = actual.get(index);
      if (actualType != null
          && !canonicalWorkspaceType(expected.get(index))
              .equals(canonicalWorkspaceType(actualType))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns normalized invocation argument types; unresolved and Spoon {@code <nulltype>} values
   * are represented as {@code null}.
   */
  private static List<String> invocationArgumentTypes(CtInvocation<?> invocation) {
    List<String> result = new ArrayList<>();
    for (CtExpression<?> argument : invocation.getArguments()) {
      CtTypeReference<?> type = argument.getType();
      String qualifiedName = type == null ? null : type.getQualifiedName();
      // Spoon represents null literals as the synthetic type "<nulltype>".
      result.add(
          qualifiedName == null || qualifiedName.isBlank() || qualifiedName.startsWith("<")
              ? null
              : JavaSourceNames.canonicalType(type.toString()));
    }
    return result;
  }

  /**
   * Returns legacy parameters only when one signature and at most one concrete target use the
   * method name.
   */
  private List<String> unambiguousLegacyParameters(String methodName) {
    List<String> parameters = rewriteRegistry.unambiguousLegacyParameters(methodName);
    if (parameters == null) {
      return null;
    }
    return rewriteRegistry.concreteTargetsNamed(methodName) <= 1 ? parameters : null;
  }

  /**
   * Resolves an invocation owner from its executable, its explicit target, or finally the lexical
   * enclosing type.
   */
  private static String ownerName(CtInvocation<?> invocation) {
    String executableOwner = null;
    try {
      if (invocation.getExecutable() != null
          && invocation.getExecutable().getDeclaringType() != null) {
        executableOwner = invocation.getExecutable().getDeclaringType().getQualifiedName();
      }
    } catch (RuntimeException ignored) {
      // Spoon may not resolve declaring types in no-classpath mode.
    }
    String targetOwner = null;
    try {
      if (invocation.getTarget() != null && invocation.getTarget().getType() != null) {
        targetOwner = invocation.getTarget().getType().getQualifiedName();
      }
    } catch (RuntimeException ignored) {
      // Fall through to the lexical owner.
    }
    if (isQualifiedOwner(targetOwner) && !isQualifiedOwner(executableOwner)) {
      return targetOwner;
    }
    if (executableOwner != null && !executableOwner.isBlank()) {
      return executableOwner;
    }
    if (targetOwner != null && !targetOwner.isBlank()) {
      return targetOwner;
    }
    CtType<?> parent = invocation.getParent(CtType.class);
    return parent == null ? null : parent.getQualifiedName();
  }

  private static boolean isQualifiedOwner(String owner) {
    return owner != null && owner.contains(".");
  }

  /** Removes bodies from non-default, non-static, non-private interface methods. */
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

  /**
   * Compares executable owners exactly. An unqualified no-classpath owner is resolved only when the
   * loaded model proves that its simple name identifies one declaration.
   */
  private boolean sameOwnerTypeName(String first, String second) {
    if (first == null || second == null) {
      return false;
    }
    String normalizedFirst = first.replace('$', '.');
    String normalizedSecond = second.replace('$', '.');
    if (normalizedFirst.contains(".") && normalizedSecond.contains(".")) {
      return normalizedFirst.equals(normalizedSecond);
    }
    String simpleFirst = JavaSourceNames.simpleName(normalizedFirst);
    String simpleSecond = JavaSourceNames.simpleName(normalizedSecond);
    if (!simpleFirst.equals(simpleSecond)) {
      return false;
    }
    List<String> modelOwners =
        workspace.model().getAllTypes().stream()
            .filter(type -> simpleFirst.equals(type.getSimpleName()))
            .map(CtType::getQualifiedName)
            .distinct()
            .toList();
    if (modelOwners.size() != 1) {
      return false;
    }
    String resolved = modelOwners.get(0).replace('$', '.');
    return (!normalizedFirst.contains(".") || resolved.equals(normalizedFirst))
        && (!normalizedSecond.contains(".") || resolved.equals(normalizedSecond));
  }

  /** Resolves unqualified type leaves only from unique model declarations or direct java.lang. */
  private String canonicalWorkspaceType(String source) {
    return JavaSourceNames.canonicalType(
        JavaSourceNames.replaceTypeNames(
            source,
            reference -> {
              if (reference.qualified()) {
                return Optional.empty();
              }
              List<String> modelTypes =
                  workspace.model().getAllTypes().stream()
                      .filter(type -> reference.simpleName().equals(type.getSimpleName()))
                      .map(CtType::getQualifiedName)
                      .distinct()
                      .toList();
              if (modelTypes.size() == 1) {
                return Optional.of(modelTypes.get(0));
              }
              String javaLang = "java.lang." + reference.simpleName();
              try {
                Class.forName(
                    javaLang, false, SpoonExecutableRepairService.class.getClassLoader());
                return Optional.of(javaLang);
              } catch (ClassNotFoundException | LinkageError ignored) {
                return Optional.empty();
              }
            }));
  }

  /** Method declaration and invocations resolved to it before the rename. */
  record MethodRename(CtMethod<?> target, List<CtInvocation<?>> invocations) {}
}
