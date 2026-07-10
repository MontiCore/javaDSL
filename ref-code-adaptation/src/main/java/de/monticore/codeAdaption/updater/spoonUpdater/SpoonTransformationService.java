package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.refactoring.Refactoring;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/** Applies all mutations to declarations and references in an already loaded Spoon model. */
final class SpoonTransformationService {
  private final SpoonWorkspace workspace;
  private final SpoonElementResolver resolver;
  private Map<String, String> groupingMappings = Collections.emptyMap();
  private final Map<String, Set<List<String>>> legacyMethodSignatures = new LinkedHashMap<>();
  private final Map<StableElementKey, StableElementKey> methodRewrites = new LinkedHashMap<>();
  private Map<String, List<CtTypeReference<?>>> typeReferencesBySimpleName;
  private Map<String, Set<String>> typeDeclarationsBySimpleName;

  SpoonTransformationService(SpoonWorkspace workspace, SpoonElementResolver resolver) {
    this.workspace = workspace;
    this.resolver = resolver;
  }

  void reset() {
    groupingMappings = Collections.emptyMap();
    legacyMethodSignatures.clear();
    methodRewrites.clear();
    typeReferencesBySimpleName = null;
    typeDeclarationsBySimpleName = null;
  }

  void setGroupingMappings(Map<String, String> mappings) {
    groupingMappings =
        mappings == null || mappings.isEmpty()
            ? Collections.emptyMap()
            : new LinkedHashMap<>(mappings);
  }

  void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    if (methodName != null && !methodName.isBlank() && parameterTypes != null) {
      legacyMethodSignatures
          .computeIfAbsent(methodName, ignored -> new java.util.LinkedHashSet<>())
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

  void prepareForPrint() {
    applyGroupingToModel();
    applyConcreteMethodSignatures();
    enforceInterfaceMethodBodies();
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

  void updateType(ASTTypeDeclaration source, String newName) {
    CtType<?> target = resolver.getSpoonType(source);
    if (!target.getSimpleName().equals(newName)) {
      Refactoring.changeTypeName(target, newName);
    }
  }

  void updateMethod(
      ASTTypeDeclaration sourceType, ASTMethodDeclaration sourceMethod, String newName) {
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
    Refactoring.changeMethodName(target, newName);
    invocations.forEach(invocation -> invocation.getExecutable().setSimpleName(newName));
  }

  private boolean invokesMethodOn(
      CtInvocation<?> invocation, CtMethod<?> target, CtType<?> owner) {
    try {
      if (invocation.getExecutable().getDeclaration() == target) {
        return true;
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

  void updateField(ASTTypeDeclaration sourceType, ASTFieldDeclaration sourceField, String newName) {
    String sourceName = sourceField.getVariableDeclarator(0).getDeclarator().getName();
    CtVariable<?> field = resolver.getSpoonType(sourceType).getField(sourceName);
    if (field == null) {
      Log.warn(
          "Cannot rename field '"
              + sourceName
              + "' in "
              + sourceType.getName()
              + " because no Spoon target exists; completed-member projection may add it later.");
      return;
    }
    renameVariable(field, newName);
  }

  void updateAssociationRole(
      ASTTypeDeclaration sourceType, String sourceRole, String concreteRole) {
    if (sourceRole == null || concreteRole == null || sourceRole.equals(concreteRole)) {
      return;
    }
    CtType<?> owner = resolver.getSpoonType(sourceType);
    for (CtFieldAccess<?> access : owner.getElements(new TypeFilter<>(CtFieldAccess.class))) {
      if (access.getParent(CtType.class) == owner
          && access.getVariable() != null
          && sourceRole.equals(access.getVariable().getSimpleName())) {
        access.getVariable().setSimpleName(concreteRole);
      }
    }
  }

  void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName) {
    String sourceName = JavaLoader.print(supertype);
    CtType<?> spoonType = resolver.getSpoonType(type);
    CtTypeReference<?> superclass = spoonType.getSuperclass();
    if (superclass != null && superclass.getSimpleName().equals(sourceName)) {
      workspace.rewriteTypeReferenceName(superclass, newName);
      return;
    }
    for (CtTypeReference<?> superInterface : spoonType.getSuperInterfaces()) {
      if (superInterface.getSimpleName().equals(sourceName)) {
        workspace.rewriteTypeReferenceName(superInterface, newName);
      }
    }
  }

  void updateLocalVariable(
      ASTTypeDeclaration sourceType,
      ASTMethodDeclaration sourceMethod,
      ASTLocalVariableDeclaration sourceVariable,
      String newName) {
    CtMethod<?> method = resolver.getSpoonMethod(sourceType, sourceMethod);
    String sourceName = sourceVariable.getVariableDeclarator(0).getDeclarator().getName();
    List<CtLocalVariable<?>> matches = method.getElements(Objects::nonNull);
    matches.removeIf(variable -> !sourceName.equals(variable.getSimpleName()));
    CtLocalVariable<?> target =
        selectByPosition(matches, sourceVariable.get_SourcePositionStart().getLine(), sourceName);
    renameVariable(target, newName);
  }

  void updateMethodParameter(
      ASTTypeDeclaration sourceType,
      ASTMethodDeclaration sourceMethod,
      ASTFormalParameter sourceParameter,
      String newName) {
    CtMethod<?> method = resolver.getSpoonMethod(sourceType, sourceMethod);
    String sourceName = sourceParameter.getDeclarator().getName();
    List<CtParameter<?>> parameters =
        method.getParameters().stream()
            .filter(parameter -> sourceName.equals(parameter.getSimpleName()))
            .collect(Collectors.toList());
    if (!parameters.isEmpty()) {
      renameVariable(selectUnique(parameters, sourceName), newName);
      return;
    }
    List<CtLocalVariable<?>> locals = method.getElements(Objects::nonNull);
    locals.removeIf(variable -> !sourceName.equals(variable.getSimpleName()));
    CtLocalVariable<?> target =
        selectByPosition(locals, sourceParameter.get_SourcePositionStart().getLine(), sourceName);
    renameVariable(target, newName);
  }

  void updateCDType(ASTCDType type, String newName) {
    indexTypeReferences();
    List<? extends CtTypeReference<?>> candidates =
        typeReferencesBySimpleName.getOrDefault(type.getName(), List.of());
    if (candidates.isEmpty()) {
      return;
    }
    Set<String> modelDeclarations =
        typeDeclarationsBySimpleName.getOrDefault(type.getName(), Set.of());
    if (modelDeclarations.size() > 1) {
      throw new IllegalStateException(
          "Ambiguous source type '"
              + type.getName()
              + "' is declared by multiple adapter types "
              + modelDeclarations);
    }
    String modelIdentity =
        modelDeclarations.isEmpty() ? null : modelDeclarations.iterator().next();
    String modelPackage =
        modelIdentity == null || !modelIdentity.contains(".")
            ? ""
            : modelIdentity.substring(0, modelIdentity.lastIndexOf('.'));
    List<? extends CtTypeReference<?>> selected =
        candidates.stream()
            .filter(
                reference -> {
                  Optional<String> identity = resolvedTypeIdentity(reference);
                  if (modelIdentity != null) {
                    if (identity.isPresent()) {
                      return modelIdentity.equals(identity.get());
                    }
                    CtType<?> owner = reference.getParent(CtType.class);
                    String ownerPackage =
                        owner == null || owner.getPackage() == null
                            ? ""
                            : owner.getPackage().getQualifiedName();
                    return modelPackage.equals(ownerPackage);
                  }
                  if (identity.isEmpty()) {
                    return true;
                  }
                  // In no-classpath mode Spoon still gives unresolved same-package references a
                  // qualified name (for example Concrete.Role). Those are adapter identities and
                  // must be rewritten. A genuinely resolved foreign type such as java.util.Date
                  // has a different package and must remain untouched.
                  CtType<?> owner = reference.getParent(CtType.class);
                  String ownerPackage =
                      owner == null || owner.getPackage() == null
                          ? ""
                          : owner.getPackage().getQualifiedName();
                  String expectedIdentity =
                      ownerPackage.isEmpty()
                          ? type.getName()
                          : ownerPackage + "." + type.getName();
                  return expectedIdentity.equals(identity.get());
                })
            .toList();
    if (selected.isEmpty()) {
      return;
    }
    if (modelIdentity == null) {
      Set<String> ownerPackages =
          selected.stream()
              .map(reference -> reference.getParent(CtType.class))
              .filter(Objects::nonNull)
              .map(owner -> owner.getPackage() == null ? "" : owner.getPackage().getQualifiedName())
              .collect(Collectors.toSet());
      if (ownerPackages.size() > 1) {
        throw new IllegalStateException(
            "Ambiguous unresolved type '"
                + type.getName()
                + "' occurs in multiple packages "
                + ownerPackages);
      }
    }
    for (CtTypeReference<?> reference : selected) {
      workspace.rewriteTypeReferenceName(reference, newName);
    }
  }

  private void indexTypeReferences() {
    if (typeReferencesBySimpleName != null) {
      return;
    }
    typeReferencesBySimpleName = new LinkedHashMap<>();
    for (CtTypeReference<?> reference :
        workspace.model().getElements(new TypeFilter<>(CtTypeReference.class))) {
      typeReferencesBySimpleName
          .computeIfAbsent(reference.getSimpleName(), ignored -> new ArrayList<>())
          .add(reference);
    }
    typeDeclarationsBySimpleName = new LinkedHashMap<>();
    for (CtType<?> declaration : workspace.model().getAllTypes()) {
      typeDeclarationsBySimpleName
          .computeIfAbsent(
              declaration.getSimpleName(), ignored -> new java.util.LinkedHashSet<>())
          .add(declaration.getQualifiedName());
    }
  }

  private void applyConcreteMethodSignatures() {
    if (legacyMethodSignatures.isEmpty() && methodRewrites.isEmpty()) {
      return;
    }
    for (CtInvocation<?> invocation :
        workspace.model().getElements(new TypeFilter<>(CtInvocation.class))) {
      String methodName =
          invocation.getExecutable() == null ? null : invocation.getExecutable().getSimpleName();
      StableElementKey target = findConcreteRewriteTarget(invocation, methodName);
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
      CtInvocation<?> invocation, String methodName) {
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
          || !ownerMatches(owner, reference, concrete)) {
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

  private boolean ownerMatches(
      String owner, StableElementKey reference, StableElementKey concrete) {
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
      // Spoon represents null literals as the synthetic type "<nulltype>". It is compatible with
      // any reference parameter and must not be sent through the Java type parser.
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

  private void applyGroupingToModel() {
    if (groupingMappings.isEmpty()) {
      return;
    }
    Set<CtType<?>> concreteDeclarations =
        workspace.model().getAllTypes().stream()
            .filter(type -> mappingFor(type.getReference()) != null)
            .collect(Collectors.toCollection(HashSet::new));
    for (CtTypeReference<?> reference :
        workspace.model().getElements(new TypeFilter<>(CtTypeReference.class))) {
      String replacement = mappingFor(reference);
      if (replacement == null) {
        continue;
      }
      CtType<?> owner = reference.getParent(CtType.class);
      if (owner != null
          && (concreteDeclarations.contains(owner)
              || (owner.getSimpleName() != null && owner.getSimpleName().endsWith("Builder")))) {
        continue;
      }
      if (!replacement.equals(reference.getSimpleName())) {
        workspace.rewriteTypeReferenceName(reference, replacement);
      }
    }
  }

  private String mappingFor(CtTypeReference<?> reference) {
    String qualifiedName = reference.getQualifiedName();
    if (qualifiedName != null && groupingMappings.containsKey(qualifiedName)) {
      return groupingMappings.get(qualifiedName);
    }
    String simpleName = reference.getSimpleName();
    if (!groupingMappings.containsKey(simpleName)) {
      return null;
    }
    Set<String> qualifiedKeys =
        groupingMappings.keySet().stream()
            .filter(key -> key.endsWith("." + simpleName))
            .collect(Collectors.toSet());
    if (!qualifiedKeys.isEmpty()) {
      return qualifiedKeys.contains(qualifiedName) ? groupingMappings.get(qualifiedName) : null;
    }
    List<CtType<?>> declarations =
        workspace.model().getAllTypes().stream()
            .filter(type -> simpleName.equals(type.getSimpleName()))
            .toList();
    if (declarations.size() != 1) {
      return null;
    }
    CtType<?> declaration = declarations.get(0);
    Optional<String> identity = resolvedTypeIdentity(reference);
    if (identity.isPresent() && !declaration.getQualifiedName().equals(identity.get())) {
      return null;
    }
    if (identity.isEmpty()) {
      CtType<?> owner = reference.getParent(CtType.class);
      String ownerPackage =
          owner == null || owner.getPackage() == null ? "" : owner.getPackage().getQualifiedName();
      String declarationPackage =
          declaration.getPackage() == null ? "" : declaration.getPackage().getQualifiedName();
      if (!ownerPackage.equals(declarationPackage)) {
        return null;
      }
    }
    return groupingMappings.get(simpleName);
  }

  private static Optional<String> resolvedTypeIdentity(CtTypeReference<?> reference) {
    try {
      CtType<?> declaration = reference.getTypeDeclaration();
      if (declaration != null && declaration.getQualifiedName() != null) {
        return Optional.of(declaration.getQualifiedName());
      }
    } catch (RuntimeException ignored) {
      // no-classpath references often have no declaration; retain qualified metadata below.
    }
    String qualifiedName = reference.getQualifiedName();
    if (qualifiedName == null
        || qualifiedName.isBlank()
        || qualifiedName.equals(reference.getSimpleName())) {
      return Optional.empty();
    }
    return Optional.of(qualifiedName);
  }

  private static <T> T selectUnique(List<T> matches, String name) {
    if (matches.size() != 1) {
      throw new IllegalStateException(
          "Expected exactly one Spoon declaration named '" + name + "' but found " + matches.size());
    }
    return matches.get(0);
  }

  private static <T extends CtVariable<?>> T selectByPosition(
      List<T> matches, int sourceLine, String name) {
    if (matches.size() == 1) {
      return matches.get(0);
    }
    List<T> sameLine =
        matches.stream()
            .filter(variable -> variable.getPosition().isValidPosition())
            .filter(variable -> variable.getPosition().getLine() == sourceLine)
            .collect(Collectors.toList());
    return selectUnique(sameLine, name + " at line " + sourceLine);
  }

  private static void renameVariable(CtVariable<?> variable, String newName) {
    CtRenameGenericVariableRefactoring refactoring = new CtRenameGenericVariableRefactoring();
    refactoring.setTarget(variable).setNewName(newName).refactor();
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
}
