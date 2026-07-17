package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.cdbasis._ast.ASTCDType;
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
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/**
 * Applies declaration, variable, grouping, and type-reference mutations to an already loaded Spoon
 * model.
 *
 * <p>Creation/removal remains in {@link SpoonGenerationService}; executable overload and invocation
 * repair remains in {@link SpoonExecutableRepairService}. Keeping these phases separate avoids one
 * stateful catch-all service and makes their ordering explicit.
 */
final class SpoonTransformationService {
  private final SpoonWorkspace workspace;
  private final SpoonElementResolver resolver;
  private final SpoonExecutableRepairService executableRepairs;
  private Map<String, String> groupingMappings = Collections.emptyMap();
  private Map<String, List<CtTypeReference<?>>> typeReferencesBySimpleName;
  private Map<String, Set<String>> typeDeclarationsBySimpleName;

  SpoonTransformationService(
      SpoonWorkspace workspace,
      SpoonElementResolver resolver,
      SpoonExecutableRepairService executableRepairs) {
    this.workspace = workspace;
    this.resolver = resolver;
    this.executableRepairs = executableRepairs;
  }

  void reset() {
    groupingMappings = Collections.emptyMap();
    typeReferencesBySimpleName = null;
    typeDeclarationsBySimpleName = null;
  }

  void setGroupingMappings(Map<String, String> mappings) {
    groupingMappings =
        mappings == null || mappings.isEmpty()
            ? Collections.emptyMap()
            : new LinkedHashMap<>(mappings);
  }

  void prepareForPrint() {
    applyGroupingToModel();
    executableRepairs.prepareForPrint(groupingMappings);
  }

  void updateType(ASTTypeDeclaration source, String newName) {
    CtType<?> target = resolver.getSpoonType(source);
    if (!target.getSimpleName().equals(newName)) {
      Refactoring.changeTypeName(target, newName);
    }
  }

  void updateMethod(
      ASTTypeDeclaration sourceType, ASTMethodDeclaration sourceMethod, String newName) {
    SpoonExecutableRepairService.MethodRename rename =
        executableRepairs.planMethodRename(sourceType, sourceMethod);
    Refactoring.changeMethodName(rename.target(), newName);
    executableRepairs.renameInvocations(rename, newName);
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
      rewriteTypeReferenceName(superclass, newName);
      return;
    }
    for (CtTypeReference<?> superInterface : spoonType.getSuperInterfaces()) {
      if (superInterface.getSimpleName().equals(sourceName)) {
        rewriteTypeReferenceName(superInterface, newName);
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
      rewriteTypeReferenceName(reference, newName);
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
      if (wouldCollapseOverload(reference)) {
        continue;
      }
      if (!replacement.equals(reference.getSimpleName())) {
        rewriteTypeReferenceName(reference, replacement);
      }
    }
  }

  /**
   * Keeps concrete parameter types when replacing them by a grouping type would erase overload
   * identity. This is required for overload sets introduced by CD completion, such as Visitor
   * methods for several concrete node incarnations.
   */
  private boolean wouldCollapseOverload(CtTypeReference<?> reference) {
    CtParameter<?> parameter = reference.getParent(CtParameter.class);
    if (parameter == null || parameter.getType() != reference) {
      return false;
    }
    CtMethod<?> method = parameter.getParent(CtMethod.class);
    CtType<?> owner = method == null ? null : method.getParent(CtType.class);
    if (owner == null) {
      return false;
    }
    String groupedSignature = groupedSignature(method);
    return owner.getMethods().stream()
        .filter(candidate -> candidate != method)
        .anyMatch(candidate -> groupedSignature.equals(groupedSignature(candidate)));
  }

  private String groupedSignature(CtMethod<?> method) {
    return method.getSimpleName()
        + "("
        + method.getParameters().stream()
            .map(CtParameter::getType)
            .map(
                type -> {
                  String grouping = mappingFor(type);
                  return grouping == null
                      ? type.getSimpleName()
                      : JavaSourceNames.simpleName(grouping);
                })
            .collect(Collectors.joining(","))
        + ")";
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

  /** Rewrites a type reference while retaining valid package identity for simple replacements. */
  private void rewriteTypeReferenceName(CtTypeReference<?> reference, String newName) {
    if (reference == null || newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("Type reference and new name must be present");
    }
    String simpleName = JavaSourceNames.simpleName(newName);
    if (newName.contains(".")) {
      CtTypeReference<?> replacement = workspace.createTypeReference(newName);
      reference.setPackage(replacement.getPackage());
      reference.setDeclaringType(replacement.getDeclaringType());
    }
    // For a simple replacement, retain the original package identity. Turning entity.User into
    // an unresolved simple Professor produces an invalid import that cleanup can only remove.
    reference.setSimpleName(simpleName);
    reference.setSimplyQualified(true);
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

}
