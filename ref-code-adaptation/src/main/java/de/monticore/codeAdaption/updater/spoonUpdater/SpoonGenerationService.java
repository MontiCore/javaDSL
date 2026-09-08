package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtEnum;
import spoon.reflect.declaration.CtEnumValue;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.refactoring.Refactoring;

/**
 * Creates, clones, completes, and removes Spoon declarations and constructs supported method
 * bodies.
 *
 * <p>It does not rename existing declarations or rewrite references; those mutations belong to
 * {@link SpoonTransformationService}.
 */
final class SpoonGenerationService {
  private final SpoonWorkspace workspace;
  private final SpoonElementResolver resolver;

  SpoonGenerationService(SpoonWorkspace workspace, SpoonElementResolver resolver) {
    this.workspace = workspace;
    this.resolver = resolver;
  }

  void addField(
      ASTTypeDeclaration targetType,
      ASTFieldDeclaration templateField,
      String newName,
      String newType,
      boolean isStatic) {
    requireName(newName, "Field name");
    requireName(newType, "Field type");
    CtType<?> spoonType = resolver.getSpoonType(targetType);
    CtField<?> existingField = spoonType.getField(newName);
    if (existingField != null) {
      existingField.setType(workspace.createTypeReference(newType));
      setStatic(existingField, isStatic);
      return;
    }
    String templateName =
        templateField == null
            ? null
            : templateField.getVariableDeclarator(0).getDeclarator().getName();
    CtField<?> sourceField = templateName == null ? null : spoonType.getField(templateName);
    CtField<?> clone =
        sourceField == null ? workspace.factory().Core().createField() : sourceField.clone();
    clone.setSimpleName(newName);
    clone.setType(workspace.createTypeReference(newType));
    setStatic(clone, isStatic);
    spoonType.addField(clone);
  }

  void addType(ASTTypeDeclaration templateType, String newName) {
    requireName(newName, "Type name");
    CtType<?> source = resolver.getSpoonType(templateType);
    CtType<?> existing =
        source.getPackage() == null ? null : source.getPackage().getType(newName);
    if (existing != null && existing != source) {
      return;
    }
    // Every adaptation selection owns an isolated source tree. Transforming that run's template
    // avoids leaking the unadapted template beside the generated type and eliminates merge
    // collisions between otherwise independent incarnation runs.
    Refactoring.changeTypeName(source, newName);
    resolver.cacheType(templateType, source);
  }

  void addSuperType(
      ASTTypeDeclaration targetType, String superTypeName, boolean interfaceType) {
    requireName(superTypeName, "Supertype name");
    CtType<?> target = resolver.getSpoonType(targetType);
    CtTypeReference<?> reference = workspace.createTypeReference(superTypeName);
    if (interfaceType || target.isInterface()) {
      boolean present =
          target.getSuperInterfaces().stream()
              .anyMatch(existing -> sameSuperInterface(existing, reference, target));
      if (!present) {
        target.addSuperInterface(reference);
      }
    } else {
      target.setSuperclass(reference);
    }
  }

  /** Compares interfaces by resolved Java identity. */
  private boolean sameSuperInterface(
      CtTypeReference<?> existing, CtTypeReference<?> requested, CtType<?> target) {
    return superTypeIdentity(existing, target).equals(superTypeIdentity(requested, target));
  }

  private String superTypeIdentity(CtTypeReference<?> reference, CtType<?> target) {
    try {
      CtType<?> declaration = reference.getTypeDeclaration();
      if (declaration != null && declaration.getQualifiedName() != null) {
        return declaration.getQualifiedName().replace('$', '.');
      }
    } catch (RuntimeException ignored) {
      // Resolve no-classpath references from the loaded model below.
    }
    String qualifiedName = reference.getQualifiedName();
    if (qualifiedName != null && qualifiedName.contains(".")) {
      return qualifiedName.replace('$', '.');
    }
    List<String> modelMatches =
        workspace.model().getAllTypes().stream()
            .filter(type -> reference.getSimpleName().equals(type.getSimpleName()))
            .map(CtType::getQualifiedName)
            .distinct()
            .toList();
    if (modelMatches.size() == 1) {
      return modelMatches.get(0).replace('$', '.');
    }
    String packageName =
        target.getPackage() == null ? "" : target.getPackage().getQualifiedName();
    return packageName.isEmpty()
        ? reference.getSimpleName()
        : packageName + "." + reference.getSimpleName();
  }

  /** Reconciles a generated Java class modifier with the authoritative target CD type. */
  void setTypeAbstract(ASTTypeDeclaration targetType, boolean isAbstract) {
    CtType<?> target = resolver.getSpoonType(targetType);
    if (target.isInterface()) {
      return;
    }
    if (isAbstract) {
      target.addModifier(ModifierKind.ABSTRACT);
    } else {
      target.removeModifier(ModifierKind.ABSTRACT);
    }
  }

  void addEnumConstant(
      ASTTypeDeclaration targetType, String constantName, int expectedIndex) {
    requireName(constantName, "Enum constant name");
    CtType<?> target = resolver.getSpoonType(targetType);
    if (!(target instanceof CtEnum<?> enumType)) {
      throw new IllegalStateException(target.getQualifiedName() + " is not an enum");
    }
    boolean present =
        enumType.getEnumValues().stream()
            .anyMatch(value -> constantName.equals(value.getSimpleName()));
    if (!present) {
      CtEnumValue value = workspace.factory().Core().createEnumValue();
      value.setSimpleName(constantName);
      List<CtEnumValue<?>> values = new ArrayList<>(enumType.getEnumValues());
      values.add(Math.min(Math.max(expectedIndex, 0), values.size()), value);
      enumType.setEnumValues(values);
    }
  }

  void addMethod(
      ASTTypeDeclaration targetType,
      ASTMethodDeclaration templateMethod,
      String newName,
      List<String> parameterTypes,
      List<String> parameterNames,
      String returnType,
      boolean isStatic,
      MethodBodySpec methodBody) {
    validateMethodInput(newName, parameterTypes, parameterNames);
    Objects.requireNonNull(methodBody, "Method body specification must not be null");
    if (methodBody.kind() == MethodBodySpec.Kind.INTERFACE_CONTRACT && isStatic) {
      throw new IllegalStateException("An interface contract implementation cannot be static");
    }
    CtType<?> spoonType = resolver.getSpoonType(targetType);
    CtMethod<?> existingMethod = findMethod(spoonType, newName, parameterTypes);
    if (existingMethod != null) {
      if (methodBody.kind() == MethodBodySpec.Kind.INTERFACE_CONTRACT) {
        completeExistingInterfaceContract(existingMethod, returnType);
        return;
      }
      existingMethod.setType(workspace.createTypeReference(returnType));
      setStatic(existingMethod, isStatic);
      return;
    }
    if (methodBody.kind() == MethodBodySpec.Kind.INTERFACE_CONTRACT
        && hasInheritedInterfaceImplementation(
            spoonType, newName, parameterTypes, returnType)) {
      return;
    }
    CtMethod<?> clone =
        cloneConfiguredMethod(
            targetType,
            templateMethod,
            newName,
            parameterTypes,
            parameterNames,
            returnType,
            hasReplacementBody(methodBody));
    setStatic(clone, isStatic);
    if (methodBody.kind() == MethodBodySpec.Kind.INTERFACE_CONTRACT) {
      clone.addModifier(ModifierKind.PUBLIC);
      clone.removeModifier(ModifierKind.ABSTRACT);
    }
    applyMethodBodySpec(targetType, clone, methodBody);
    spoonType.addMethod(clone);
  }

  /**
   * Turns an existing declaration into a legal concrete interface implementation without
   * replacing a handwritten body. Signature conflicts are rejected before visibility/body repair.
   */
  private void completeExistingInterfaceContract(
      CtMethod<?> existingMethod, String contractReturnType) {
    if (existingMethod.hasModifier(ModifierKind.STATIC)) {
      throw new IllegalStateException(
          "Static method "
              + existingMethod.getSignature()
              + " cannot implement an interface contract");
    }
    CtTypeReference<?> existingReturn = existingMethod.getType();
    CtTypeReference<?> contractReturn = workspace.createTypeReference(contractReturnType);
    if (!isCompatibleInterfaceReturn(existingReturn, contractReturn)) {
      throw new IllegalStateException(
          "Method "
              + existingMethod.getSignature()
              + " returns "
              + typeName(existingReturn)
              + " but interface contract requires "
              + typeName(contractReturn));
    }
    existingMethod.removeModifier(ModifierKind.PRIVATE);
    existingMethod.removeModifier(ModifierKind.PROTECTED);
    existingMethod.addModifier(ModifierKind.PUBLIC);
    existingMethod.removeModifier(ModifierKind.ABSTRACT);
    if (existingMethod.getBody() == null) {
      existingMethod.setBody(safeDefaultBody(typeName(existingReturn)));
    }
  }

  /** Accepts identical returns and Java covariance, but never mixes primitive and reference types. */
  private boolean isCompatibleInterfaceReturn(
      CtTypeReference<?> actualReturn, CtTypeReference<?> expectedReturn) {
    if (actualReturn == null || expectedReturn == null) {
      return false;
    }
    if (sameType(actualReturn, expectedReturn)) {
      return true;
    }
    if (isPrimitiveOrVoid(actualReturn) || isPrimitiveOrVoid(expectedReturn)) {
      return false;
    }
    if ("Object".equals(expectedReturn.getSimpleName())) {
      return true;
    }
    try {
      return actualReturn.isSubtypeOf(expectedReturn);
    } catch (RuntimeException ignored) {
      return false;
    }
  }

  private boolean sameType(CtTypeReference<?> first, CtTypeReference<?> second) {
    String firstQualified = first.getQualifiedName();
    String secondQualified = second.getQualifiedName();
    if (Objects.equals(firstQualified, secondQualified)) {
      return true;
    }
    return first.getSimpleName().equals(second.getSimpleName())
        && (first.isSimplyQualified() || second.isSimplyQualified());
  }

  private boolean isPrimitiveOrVoid(CtTypeReference<?> type) {
    return type.isPrimitive() || "void".equals(type.getSimpleName());
  }

  private String typeName(CtTypeReference<?> type) {
    if (type == null) {
      return "Object";
    }
    String qualifiedName = type.getQualifiedName();
    return qualifiedName == null || qualifiedName.isBlank() ? type.getSimpleName() : qualifiedName;
  }

  private boolean requiresSignatureOnlyMethod(
      ASTTypeDeclaration targetType, ASTMethodDeclaration templateMethod) {
    CtType<?> target = resolver.getSpoonType(targetType);
    if (target.isInterface()) {
      return true;
    }
    if (templateMethod == null) {
      return false;
    }
    return resolver
        .findSpoonMethod(targetType, templateMethod)
        .map(method -> method.hasModifier(ModifierKind.ABSTRACT))
        .orElse(false);
  }

  void removeField(ASTTypeDeclaration targetType, ASTFieldDeclaration field) {
    String name = field.getVariableDeclarator(0).getDeclarator().getName();
    CtField<?> target = resolver.getSpoonType(targetType).getField(name);
    if (target != null) {
      target.delete();
    }
  }

  void removeMethod(ASTTypeDeclaration targetType, ASTMethodDeclaration method) {
    resolver.findSpoonMethod(targetType, method).ifPresent(CtMethod::delete);
  }

  private CtMethod<?> cloneConfiguredMethod(
      ASTTypeDeclaration targetType,
      ASTMethodDeclaration templateMethod,
      String newName,
      List<String> parameterTypes,
      List<String> parameterNames,
      String returnType,
      boolean replacementBody) {
    CtType<?> target = resolver.getSpoonType(targetType);
    CtMethod<?> clone =
        templateMethod == null
            ? workspace.factory().Core().createMethod()
            : resolver
                .findSpoonMethod(targetType, templateMethod)
                .map(CtMethod::clone)
                .orElseGet(() -> workspace.factory().Core().createMethod());
    clone.setSimpleName(newName);
    int sharedParameters = Math.min(clone.getParameters().size(), parameterNames.size());
    for (int index = 0; index < sharedParameters; index++) {
      updateParameter(
          clone,
          clone.getParameters().get(index),
          parameterNames.get(index),
          parameterTypes.get(index));
    }
    if (clone.getParameters().size() < parameterNames.size()) {
      for (int index = clone.getParameters().size(); index < parameterNames.size(); index++) {
        CtParameter<?> parameter = workspace.factory().Core().createParameter();
        parameter.setSimpleName(parameterNames.get(index));
        parameter.setType(workspace.createTypeReference(parameterTypes.get(index)));
        clone.addParameter(parameter);
      }
    } else {
      while (clone.getParameters().size() > parameterNames.size()) {
        clone.removeParameter(clone.getParameters().get(clone.getParameters().size() - 1));
      }
    }
    if (returnType != null && !returnType.isBlank()) {
      clone.setType(workspace.createTypeReference(returnType));
    }
    boolean primitiveBodyReturnsNull =
        !replacementBody
            && clone.getType() != null
            && clone.getType().isPrimitive()
            && clone.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtReturn.class))
                .stream()
                .map(CtReturn::getReturnedExpression)
                .anyMatch(
                    expression ->
                        expression == null
                            || (expression instanceof CtLiteral<?> literal
                                && literal.getValue() == null)
                            || "null".equals(expression.toString())
                            || "<nulltype>".equals(expression.toString()));
    if (requiresSignatureOnlyMethod(targetType, templateMethod)) {
      clone.setBody(null);
      if (!target.isInterface()) {
        clone.addModifier(ModifierKind.ABSTRACT);
      }
    } else if (primitiveBodyReturnsNull) {
      clone.setBody(safeDefaultBody(returnType));
    } else if (clone.getBody() == null && !"void".equals(returnType)) {
      CtBlock<?> body = workspace.factory().Core().createBlock();
      CtReturn<?> returnStatement = workspace.factory().Core().createReturn();
      returnStatement.setReturnedExpression((CtExpression) workspace.defaultExpression(returnType));
      body.addStatement(returnStatement);
      clone.setBody(body);
    } else if (clone.getBody() == null) {
      clone.setBody(workspace.factory().Core().createBlock());
    }
    return clone;
  }

  private void updateParameter(
      CtMethod<?> method, CtParameter<?> parameter, String newName, String newType) {
    String oldName = parameter.getSimpleName();
    if (!oldName.equals(newName)) {
      for (CtVariableAccess<?> access :
          method.getElements(
              new spoon.reflect.visitor.filter.TypeFilter<>(CtVariableAccess.class))) {
        if (access.getVariable() == null) {
          continue;
        }
        CtVariableReference<?> reference = access.getVariable();
        if (reference.getDeclaration() == parameter
            || (reference instanceof CtParameterReference<?>
                && reference.getDeclaration() == null
                && oldName.equals(reference.getSimpleName()))) {
          reference.setSimpleName(newName);
        }
      }
      parameter.setSimpleName(newName);
    }
    parameter.setType(workspace.createTypeReference(newType));
  }

  private CtBlock<?> safeDefaultBody(String returnType) {
    CtBlock<?> body = workspace.factory().Core().createBlock();
    if (returnType != null && !returnType.isBlank() && !"void".equals(returnType)) {
      CtReturn<?> returnStatement = workspace.factory().Core().createReturn();
      returnStatement.setReturnedExpression((CtExpression) workspace.defaultExpression(returnType));
      body.addStatement(returnStatement);
    }
    return body;
  }

  private static boolean hasReplacementBody(MethodBodySpec methodBody) {
    return methodBody.kind() != MethodBodySpec.Kind.EMPTY
        && methodBody.kind() != MethodBodySpec.Kind.INTERFACE_CONTRACT;
  }

  /** Checks inherited declarations without ever mutating the declaring supertype. */
  private boolean hasInheritedInterfaceImplementation(
      CtType<?> type, String name, List<String> parameterTypes, String returnType) {
    CtTypeReference<?> expectedReturn = workspace.createTypeReference(returnType);
    for (CtMethod<?> method : type.getAllMethods()) {
      if (method.getDeclaringType() == type || !matchesSignature(method, name, parameterTypes)) {
        continue;
      }
      if (method.hasModifier(ModifierKind.PUBLIC)
          && !method.hasModifier(ModifierKind.STATIC)
          && !method.hasModifier(ModifierKind.ABSTRACT)
          && isCompatibleInterfaceReturn(method.getType(), expectedReturn)) {
        return true;
      }
    }
    return false;
  }

  private CtMethod<?> findMethod(CtType<?> type, String name, List<String> parameterTypes) {
    for (CtMethod<?> method : type.getMethods()) {
      if (matchesSignature(method, name, parameterTypes)) {
        return method;
      }
    }
    return null;
  }

  private boolean matchesSignature(
      CtMethod<?> method, String name, List<String> parameterTypes) {
    if (!name.equals(method.getSimpleName())
        || method.getParameters().size() != parameterTypes.size()) {
      return false;
    }
    for (int index = 0; index < parameterTypes.size(); index++) {
      CtTypeReference<?> actualType = method.getParameters().get(index).getType();
      CtTypeReference<?> expectedType = workspace.createTypeReference(parameterTypes.get(index));
      if (!sameType(actualType, expectedType)) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void applyMethodBodySpec(
      ASTTypeDeclaration targetType, CtMethod<?> clone, MethodBodySpec methodBody) {
    if (methodBody.kind() == MethodBodySpec.Kind.EMPTY
        || methodBody.kind() == MethodBodySpec.Kind.INTERFACE_CONTRACT) {
      return;
    }
    if (methodBody.kind() == MethodBodySpec.Kind.SAFE_DEFAULT) {
      if (!clone.hasModifier(ModifierKind.ABSTRACT)) {
        clone.setBody(safeDefaultBody(typeName(clone.getType())));
      }
      return;
    }
    CtBlock<?> body = workspace.factory().Core().createBlock();
    clone.setBody(body);
    switch (methodBody.kind()) {
      case ASSIGN_FIELD_AND_RETURN_THIS:
        requireName(methodBody.fieldName(), "Assigned field name");
        requireName(methodBody.parameterName(), "Assigned parameter name");
        CtParameter<?> parameter =
            clone.getParameters().stream()
                .filter(value -> value.getSimpleName().equals(methodBody.parameterName()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Generated body references missing parameter '"
                                + methodBody.parameterName()
                                + "'"));
        CtAssignment assignment = workspace.factory().Core().createAssignment();
        assignment.setAssigned(
            (CtExpression)
                workspace
                    .factory()
                    .Code()
                    .createCodeSnippetExpression("this." + methodBody.fieldName()));
        CtVariableReference<?> variableReference = parameter.getReference();
        assignment.setAssignment(
            (CtExpression) workspace.factory().Code().createVariableRead(variableReference, false));
        body.addStatement(assignment);
        CtReturn setterReturn = workspace.factory().Core().createReturn();
        setterReturn.setReturnedExpression((CtExpression) createThisAccess(targetType));
        body.addStatement(setterReturn);
        break;
      case RETURN_NEW:
        requireName(methodBody.constructorType(), "Constructor type");
        CtConstructorCall<?> constructorCall = workspace.factory().Core().createConstructorCall();
        constructorCall.setType(workspace.createTypeReference(methodBody.constructorType()));
        for (String fieldArgument : methodBody.constructorFieldArguments()) {
          requireName(fieldArgument, "Constructor field argument");
          constructorCall.addArgument(
              workspace.factory().Code().createCodeSnippetExpression("this." + fieldArgument));
        }
        CtReturn buildReturn = workspace.factory().Core().createReturn();
        buildReturn.setReturnedExpression((CtExpression) constructorCall);
        body.addStatement(buildReturn);
        break;
      default:
        throw new IllegalArgumentException("Unsupported method body kind: " + methodBody.kind());
    }
  }

  private CtThisAccess<?> createThisAccess(ASTTypeDeclaration targetType) {
    CtType<?> target = resolver.getSpoonType(targetType);
    CtThisAccess<?> thisAccess = workspace.factory().Core().createThisAccess();
    CtTypeReference<?> typeReference =
        workspace.factory().Type().createReference(target.getSimpleName());
    try {
      CtTypeAccess<?> typeAccess = workspace.factory().Code().createTypeAccess(typeReference);
      thisAccess.setTarget(typeAccess);
    } catch (RuntimeException ignored) {
      thisAccess.setType(typeReference);
    }
    return thisAccess;
  }

  private static void validateMethodInput(
      String methodName, List<String> parameterTypes, List<String> parameterNames) {
    requireName(methodName, "Method name");
    Objects.requireNonNull(parameterTypes, "Parameter types must not be null");
    Objects.requireNonNull(parameterNames, "Parameter names must not be null");
    if (parameterTypes.size() != parameterNames.size()) {
      throw new IllegalArgumentException(
          "Parameter type/name count differs: "
              + parameterTypes.size()
              + " types and "
              + parameterNames.size()
              + " names");
    }
    parameterTypes.forEach(type -> requireName(type, "Parameter type"));
    parameterNames.forEach(name -> requireName(name, "Parameter name"));
  }

  private static void requireName(String value, String description) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(description + " must not be blank");
    }
  }

  private static void setStatic(spoon.reflect.declaration.CtModifiable declaration, boolean value) {
    if (value) {
      declaration.addModifier(ModifierKind.STATIC);
    } else {
      declaration.removeModifier(ModifierKind.STATIC);
    }
  }
}
