package de.monticore.codeAdaption.updater;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;

/***
 * this class execute the update operation in the reference code.
 */
public interface CodeUpdater {
  /***
   * set the path to the referenceCode.
   * @param path the path.
   */
  void setCodePath(Path path);

  /***
   * print the current state of the reference code as set of files.
   * @return the code as files
   */
  Set<File> printCode();

  /***
   * change the name of a type in the reference code. By the declaration
   * an all its reference an uses.
   * @param srcType the source type to replace.
   * @param newName the new name of the type.
   */
  void updateType(ASTTypeDeclaration srcType, String newName);

  /***
   * change the name of a method in the reference code. By the declaration
   * an all its reference an uses.
   * @param srcType the source-type  that contains the method-declaration.
   * @param srcMethod the source method.
   * @param newName the new name of the type.
   */
  void updateMethod(ASTTypeDeclaration srcType, ASTMethodDeclaration srcMethod, String newName);

  /***
   * change the name of a field in the reference code. By its declaration
   * an all its references an uses.
   * @param srcType the source-type that contains the field-declaration
   * @param srcField the source Field.
   * @param newName the new name of the field.
   */
  void updateField(ASTTypeDeclaration srcType, ASTFieldDeclaration srcField, String newName);

  /***
   * change the name of a local-variable in the reference code. By its declaration
   * an all its references an uses.
   * @param srcType the source-type that contains the local variable.
   * @param srcMethod the source method tha contains le local variable.
   * @param sourceVar the source local variable.
   * @param newName the new name of the local variable.
   */
  void updateLocalVariable(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTLocalVariableDeclaration sourceVar,
      String newName);

  /***
   * change the name of a local-variable in the reference code. By it declaration
   * and all its references an uses
   * @param srcType the source type that contains the local parameter
   * @param srcMethod the source method that contains le the parameter
   * @param srcParam the source parameter
   * @param newName the new name of the parameter
   */
  void updateMethodParameter(
      ASTTypeDeclaration srcType,
      ASTMethodDeclaration srcMethod,
      ASTFormalParameter srcParam,
      String newName);

  /***
   * Update all types that are not present in the reference code.
   * @param cdType type  in the reference class diagram to update in the reference code.
   * @param newName new name of the type.
   */
  void updateCDType(ASTCDType cdType, String newName);
  /***
   * set the output directory of the code by printing.
   * @param outputPath the output directory
   */
  void setOutputDirectory(Path outputPath);

  void updateSuperType(ASTTypeDeclaration type, ASTMCType supertype, String newName);
}
