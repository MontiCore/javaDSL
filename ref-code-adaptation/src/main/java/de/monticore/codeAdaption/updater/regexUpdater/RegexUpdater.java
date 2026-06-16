package de.monticore.codeAdaption.updater.regexUpdater;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;

// Fixme: RegExUpdater needs to be repaired
@Deprecated()
public class RegexUpdater implements CodeUpdater {
  protected Set<File> code;

  protected Path outputDir;

  @Override
  public void setCodePath(Path path) {
    code = JavaLoader.readJavaFile(path);
  }

  @Override
  public Set<File> printCode() {
    return code;
  }

  @Override
  public void updateType(ASTTypeDeclaration srcType, String newName) {
    updateElement(srcType.getName(), newName);
  }

  @Override
  public void updateMethod(
      ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName) {
    updateElement(srcMethod.getName(), newName);
  }

  @Override
  public void updateField(
      ASTTypeDeclaration srcType, ASTFieldDeclaration srcField, String newName) {
    String srcName = srcField.getVariableDeclarator(0).getDeclarator().getName();
    updateElement(srcName, newName);
  }

  @Override
  public void updateLocalVariable(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTLocalVariableDeclaration sourceVar,
      String newName) {
    String srcName = sourceVar.getVariableDeclarator(0).getDeclarator().getName();
    updateElement(srcName, newName);
  }

  @Override
  public void updateMethodParameter(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTFormalParameter srcParam,
      String newName) {
    String srcName = srcParam.getDeclarator().getName();
    updateElement(srcName, newName);
  }

  @Override
  public void updateCDType(ASTCDType cdType, String newName) {
    updateElement(cdType.getName(), newName);
  }

  @Override
  public void setOutputDirectory(Path outputPath) {
    this.outputDir = outputPath;
  }

  @Override
  public void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName) {
    updateElement(supertype.printType(), newName);
  }

  public void updateElement(String src, String target) {
    for (File file : code) {
      String fileText = JavaLoader.readFileContent(file);
      String updateText = fileText.replaceAll(src, target);
      JavaLoader.writeFile(Path.of(file.getAbsolutePath()), updateText);
    }
  }
}
