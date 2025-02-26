package de.monticore.codeAdaption.matcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import java.util.Optional;

public interface VariableMatcher {
  void setReferenceCD(ASTCDCompilationUnit cd);

  Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar);

  Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param);
}
