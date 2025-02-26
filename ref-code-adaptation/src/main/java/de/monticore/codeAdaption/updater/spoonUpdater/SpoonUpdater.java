package de.monticore.codeAdaption.updater.spoonUpdater;

import static de.monticore.codeAdaption.utils.JavaLoader.print;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import spoon.Launcher;
import spoon.refactoring.CtRenameGenericVariableRefactoring;
import spoon.refactoring.Refactoring;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

public class SpoonUpdater implements CodeUpdater {
  private File outputDir;
  private Launcher launcher;
  private CtModel spoonModel;
  private final Map<ASTTypeDeclaration, CtType<?>> typeMap = new HashMap<>();
  private final Map<ASTMethodDeclaration, CtMethod<?>> methodMap = new HashMap<>();

  @Override
  public void setCodePath(Path path) {
    // init spoon environment
    launcher = new Launcher();
    launcher.getEnvironment().setAutoImports(true);
    launcher.getEnvironment().setShouldCompile(true);

    // add code to the environment
    launcher.addInputResource(path.toAbsolutePath().toString());

    // build model
    launcher.buildModel();
    spoonModel = launcher.getModel();
  }

  @Override
  public Set<File> printCode() {
    launcher.setSourceOutputDirectory(outputDir);
    launcher.prettyprint();
    return JavaLoader.readJavaFile(outputDir.toPath());
  }

  @Override
  public void updateType(ASTTypeDeclaration source, String newName) {
    CtType<?> type = getSpoonType(source);
    Refactoring.changeTypeName(type, newName);
  }

  @Override
  public void updateMethod(
      ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName) {
    CtMethod<?> method = getSpoonMethod(srcType, srcMethod);
    Refactoring.changeMethodName(method, newName);
  }

  @Override
  public void updateField(
      ASTTypeDeclaration srcType, ASTFieldDeclaration srcField, String newName) {

    // get Spoon Variable
    String srcName = srcField.getVariableDeclarator(0).getDeclarator().getName();
    CtVariable<?> attribute = getSpoonType(srcType).getField(srcName);

    // perform update
    CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
    refactor.setTarget(attribute).setNewName(newName).refactor();
  }

  @Override
  public void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName) {
    String srcName = JavaLoader.print(supertype);
    CtType<?> spoonType = getSpoonType(type);

    // case super class
    CtTypeReference<?> superType = spoonType.getSuperclass();
    if (superType != null && superType.getSimpleName().equals(srcName)) {
      spoonType.getSuperclass().setSimpleName(newName);
      return;
    }

    // case super interface
    for (CtTypeReference<?> superType2 : spoonType.getSuperInterfaces()) {
      if (superType2.getSimpleName().equals(srcName)) {
        superType2.setSimpleName(newName);
      }
    }
  }

  @Override
  public void updateLocalVariable(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTLocalVariableDeclaration sourceVar,
      String newName) {

    // get spoon local-variable
    CtMethod<?> spoonMethod = getSpoonMethod(srcType, srcMethod);
    List<CtLocalVariable<?>> localVars = spoonMethod.getElements(Objects::nonNull);
    String varName = sourceVar.getVariableDeclarator(0).getDeclarator().getName();
    Optional<CtLocalVariable<?>> var =
        localVars.stream().filter(v -> v.getSimpleName().equals(varName)).findAny();
    assert var.isPresent();

    // perform update
    CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
    refactor.setTarget(var.get()).setNewName(newName).refactor();
  }

  @Override
  public void updateMethodParameter(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTFormalParameter srcParam,
      String newName) {

    // get spoon formal parameter of method
    CtMethod<?> spoonMethod = getSpoonMethod(srcType, srcMethod);
    List<CtParameter<?>> params = spoonMethod.getElements(Objects::nonNull);
    String srcVarName = srcParam.getDeclarator().getName();
    Optional<CtParameter<?>> param =
        params.stream().filter(v -> v.getSimpleName().equals(srcVarName)).findAny();

    if (param.isPresent()) {
      // perform update
      CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
      refactor.setTarget(param.get()).setNewName(newName).refactor();

    } else {

      // case formal param in for loop
      List<CtLocalVariable<?>> localVars = spoonMethod.getElements(Objects::nonNull);
      Optional<CtLocalVariable<?>> localvar =
          localVars.stream().filter(v -> v.getSimpleName().equals(srcVarName)).findAny();
      assert localvar.isPresent();

      // case formal param in for loop
      CtRenameGenericVariableRefactoring refactor = new CtRenameGenericVariableRefactoring();
      refactor.setTarget(localvar.get()).setNewName(newName).refactor();
    }
  }

  @Override
  public void updateCDType(ASTCDType cdType, String newName) {
    List<CtTypeReference<?>> refTypes = spoonModel.getElements(Objects::nonNull);

    for (CtTypeReference<?> typeRef : refTypes) {
      if (typeRef.getSimpleName().equals(cdType.getName())) {
        typeRef.setSimpleName(newName);
      }
    }

    //  for ()
  }

  @Override
  public void setOutputDirectory(Path outputPath) {
    this.outputDir = outputPath.toFile();
  }

  /***
   *get spoonType form the Spoon Model and the mcType.
   * save the found spoonType in the type map.
   */
  private CtType<?> getSpoonType(ASTTypeDeclaration mcType) {
    // cas already found
    if (typeMap.containsKey(mcType)) {
      return typeMap.get(mcType);
    }
    // search in the spoon model
    Optional<CtType<?>> type =
        spoonModel.getAllTypes().stream().filter(t -> compare(mcType, t)).findFirst();
    assert type.isPresent();
    typeMap.put(mcType, type.get());
    return type.get();
  }

  /***
   *get spoonMethod form the Spoon Model and the mcType an the mcMethod.
   * save the found spoonMethod in the type map.
   */
  public CtMethod<?> getSpoonMethod(ASTTypeDeclaration mcType, ASTMethodDeclaration mcMethod) {
    // cas method was already found
    if (methodMap.containsKey(mcMethod)) {
      return methodMap.get(mcMethod);
    }

    // search method in the spoonType
    CtType<?> spoonType = getSpoonType(mcType);
    Optional<CtMethod<?>> method =
        spoonType.getAllMethods().stream()
            .filter(spMethod -> compare(mcMethod, spMethod))
            .findFirst();

    assert method.isPresent();
    methodMap.put(mcMethod, method.get());
    return method.get();
  }

  /***
   * compare a mcType an spoonType and return true if both are identical.
   */
  protected boolean compare(ASTTypeDeclaration type, CtType<?> spoonType) {
    String fileName = type.get_SourcePositionStart().getFileName().orElse(type.getName());
    return fileName.replaceAll("\\\\", ".").endsWith(spoonType.getSimpleName() + ".java");
  }

  /***
   *compare spoonMethod and  mcMethod and return true if both are identical.
   */
  protected boolean compare(ASTMethodDeclaration mcMethod, CtMethod<?> spoonMethod) {
    // compare names
    if (!mcMethod.getName().endsWith(spoonMethod.getSimpleName())) {
      return false;
    }
    // is present parameters ?
    if (!mcMethod.getFormalParameters().isPresentFormalParameterListing()) {
      return spoonMethod.getParameters().isEmpty();
    }
    // same number of parameters ?
    List<ASTFormalParameter> mcParams =
        mcMethod.getFormalParameters().getFormalParameterListing().getFormalParameterList();
    if (spoonMethod.getParameters().size() != mcParams.size()) {
      return false;
    }
    // parameters have the same type ?
    for (int i = 0; i < spoonMethod.getParameters().size(); i++) {
      if (!(spoonMethod.getParameters().get(i).getType().getSimpleName())
          .equals(print(mcParams.get(i).getMCType()))) {
        return false;
      }
    }

    return true;
  }
}
