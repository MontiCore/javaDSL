package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
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
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.refactoring.Refactoring;

/** Generates, clones and removes Spoon declarations. */
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
    if (spoonType.getField(newName) != null) {
      return;
    }
    String templateName = templateField.getVariableDeclarator(0).getDeclarator().getName();
    CtField<?> sourceField = spoonType.getField(templateName);
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

  void addMethod(
      ASTTypeDeclaration targetType,
      ASTMethodDeclaration templateMethod,
      String newName,
      List<String> parameterTypes,
      List<String> parameterNames,
      String returnType,
      boolean isStatic,
      MethodBodySpec methodBody,
      String legacyBody) {
    validateMethodInput(newName, parameterTypes, parameterNames);
    CtType<?> spoonType = resolver.getSpoonType(targetType);
    if (hasMethod(spoonType, newName, parameterTypes)) {
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
            hasReplacementBody(methodBody, legacyBody));
    setStatic(clone, isStatic);
    if (legacyBody != null && !legacyBody.isBlank()) {
      clone.setBody(workspace.factory().Core().createBlock());
      clone.getBody().addStatement(workspace.factory().Code().createCodeSnippetStatement(legacyBody));
    } else {
      applyMethodBodySpec(targetType, clone, methodBody);
    }
    spoonType.addMethod(clone);
  }

  boolean requiresSignatureOnlyMethod(
      ASTTypeDeclaration targetType, ASTMethodDeclaration templateMethod) {
    CtType<?> target = resolver.getSpoonType(targetType);
    if (target.isInterface()) {
      return true;
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
        resolver
            .findSpoonMethod(targetType, templateMethod)
            .map(CtMethod::clone)
            .orElseGet(() -> workspace.factory().Core().createMethod());
    clone.setSimpleName(newName);
    boolean signatureArityChanged = clone.getParameters().size() != parameterNames.size();
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

  private static boolean hasReplacementBody(MethodBodySpec methodBody, String legacyBody) {
    return (legacyBody != null && !legacyBody.isBlank())
        || (methodBody != null && methodBody.kind() != MethodBodySpec.Kind.EMPTY);
  }

  private boolean hasMethod(CtType<?> type, String name, List<String> parameterTypes) {
    for (CtMethod<?> method : type.getMethods()) {
      if (!name.equals(method.getSimpleName())
          || method.getParameters().size() != parameterTypes.size()) {
        continue;
      }
      boolean same = true;
      for (int index = 0; index < parameterTypes.size(); index++) {
        CtTypeReference<?> actualType = method.getParameters().get(index).getType();
        String actual = actualType == null ? "" : actualType.getSimpleName();
        String expected = JavaSourceNames.simpleName(parameterTypes.get(index));
        if (!actual.equals(expected)) {
          same = false;
          break;
        }
      }
      if (same) {
        return true;
      }
    }
    return false;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void applyMethodBodySpec(
      ASTTypeDeclaration targetType, CtMethod<?> clone, MethodBodySpec methodBody) {
    if (methodBody == null || methodBody.kind() == MethodBodySpec.Kind.EMPTY) {
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
