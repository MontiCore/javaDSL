package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import spoon.reflect.declaration.CtType;

/** Spoon-backed facade for loading, transforming, generating and printing Java source code. */
public class SpoonUpdater implements CodeUpdater {
  private final SpoonWorkspace workspace;
  private final SpoonElementResolver elementResolver;
  private final SpoonExecutableRepairService executableRepairs;
  private final SpoonTransformationService transformations;
  private final SpoonGenerationService generation;

  public SpoonUpdater() {
    workspace = new SpoonWorkspace();
    elementResolver = new SpoonElementResolver(workspace::model);
    executableRepairs = new SpoonExecutableRepairService(workspace, elementResolver);
    transformations =
        new SpoonTransformationService(workspace, elementResolver, executableRepairs);
    generation = new SpoonGenerationService(workspace, elementResolver);
  }

  @Override
  public void setCodePath(Path path) {
    workspace.load(path);
    elementResolver.reset();
    transformations.reset();
    executableRepairs.reset();
  }

  @Override
  public Set<File> printCode() {
    transformations.prepareForPrint();
    return workspace.print();
  }

  @Override
  public void cleanCode(Path codePath) {
    workspace.clean(codePath);
  }

  @Override
  public void cleanCode(Path codePath, Map<String, String> topToPublicSelfTypes) {
    workspace.clean(codePath, topToPublicSelfTypes);
  }

  @Override
  public void setGroupingMappings(Map<String, String> mappings) {
    transformations.setGroupingMappings(mappings);
  }

  @Override
  public void registerConcreteMethodSignature(String methodName, List<String> parameterTypes) {
    executableRepairs.registerConcreteMethodSignature(methodName, parameterTypes);
  }

  @Override
  public void registerMethodRewrite(
      StableElementKey referenceMethod, StableElementKey concreteMethod) {
    executableRepairs.registerMethodRewrite(referenceMethod, concreteMethod);
  }

  @Override
  public void registerMethodRewrite(
      ASTTypeDeclaration sourceOwner,
      StableElementKey referenceMethod,
      StableElementKey concreteMethod) {
    CtType<?> sourceType = elementResolver.getSpoonType(sourceOwner);
    String referenceOwnerIdentity = sourceType.getQualifiedName();
    String concreteOwnerIdentity = concreteOwnerIdentity(sourceType, concreteMethod);
    executableRepairs.registerMethodRewrite(
        referenceMethod.withOwnerType(referenceOwnerIdentity),
        concreteMethod.withOwnerType(concreteOwnerIdentity));
  }

  private static String concreteOwnerIdentity(
      CtType<?> sourceType, StableElementKey concreteMethod) {
    String requestedOwner = concreteMethod.getOwnerType().orElse(sourceType.getSimpleName());
    if (requestedOwner.contains(".") || requestedOwner.contains("$")) {
      return requestedOwner;
    }
    if (sourceType.getDeclaringType() != null) {
      return sourceType.getDeclaringType().getQualifiedName() + "$" + requestedOwner;
    }
    String packageName =
        sourceType.getPackage() == null ? "" : sourceType.getPackage().getQualifiedName();
    return packageName.isEmpty() ? requestedOwner : packageName + "." + requestedOwner;
  }

  @Override
  public void updateType(ASTTypeDeclaration source, String newName) {
    transformations.updateType(source, newName);
  }

  @Override
  public void updateMethod(
      ASTTypeDeclaration sourceType, ASTMethodDeclaration sourceMethod, String newName) {
    transformations.updateMethod(sourceType, sourceMethod, newName);
  }

  @Override
  public void updateField(
      ASTTypeDeclaration sourceType, ASTFieldDeclaration sourceField, String newName) {
    transformations.updateField(sourceType, sourceField, newName);
  }

  @Override
  public void updateAssociationRole(
      ASTTypeDeclaration sourceType, String sourceRole, String concreteRole) {
    transformations.updateAssociationRole(sourceType, sourceRole, concreteRole);
  }

  @Override
  public void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName) {
    transformations.updateSuperType(type, supertype, newName);
  }

  @Override
  public void updateLocalVariable(
      ASTTypeDeclaration sourceType,
      ASTMethodDeclaration sourceMethod,
      ASTLocalVariableDeclaration sourceVariable,
      String newName) {
    transformations.updateLocalVariable(
        sourceType, sourceMethod, sourceVariable, newName);
  }

  @Override
  public void updateMethodParameter(
      ASTTypeDeclaration sourceType,
      ASTMethodDeclaration sourceMethod,
      ASTFormalParameter sourceParameter,
      String newName) {
    transformations.updateMethodParameter(
        sourceType, sourceMethod, sourceParameter, newName);
  }

  @Override
  public void updateCDType(ASTCDType type, String newName) {
    transformations.updateCDType(type, newName);
  }

  @Override
  public void setOutputDirectory(Path outputPath) {
    workspace.setOutputDirectory(outputPath);
  }

  @Override
  public void addField(
      ASTTypeDeclaration targetType,
      ASTFieldDeclaration templateField,
      String newName,
      String newType,
      boolean isStatic) {
    generation.addField(targetType, templateField, newName, newType, isStatic);
  }

  @Override
  public void addType(ASTTypeDeclaration templateType, String newName) {
    generation.addType(templateType, newName);
  }

  @Override
  public void addSuperType(
      ASTTypeDeclaration targetType, String superTypeName, boolean interfaceType) {
    generation.addSuperType(targetType, superTypeName, interfaceType);
  }

  @Override
  public void setTypeAbstract(ASTTypeDeclaration targetType, boolean isAbstract) {
    generation.setTypeAbstract(targetType, isAbstract);
  }

  @Override
  public void addEnumConstant(
      ASTTypeDeclaration targetType, String constantName, int expectedIndex) {
    generation.addEnumConstant(targetType, constantName, expectedIndex);
  }

  @Override
  public void addMethod(
      ASTTypeDeclaration targetType,
      ASTMethodDeclaration templateMethod,
      String newName,
      List<String> parameterTypes,
      List<String> parameterNames,
      String returnType,
      boolean isStatic,
      MethodBodySpec methodBody) {
    generation.addMethod(
        targetType,
        templateMethod,
        newName,
        parameterTypes,
        parameterNames,
        returnType,
        isStatic,
        methodBody);
  }

  @Override
  public void removeField(ASTTypeDeclaration targetType, ASTFieldDeclaration field) {
    generation.removeField(targetType, field);
  }

  @Override
  public void removeMethod(ASTTypeDeclaration targetType, ASTMethodDeclaration method) {
    generation.removeMethod(targetType, method);
  }

}
