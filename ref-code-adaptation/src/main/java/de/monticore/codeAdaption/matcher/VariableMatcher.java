package de.monticore.codeAdaption.matcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.util.Optional;

/** Strategy for matching Java local variables and formal parameters within a matched method. */
public interface VariableMatcher {
  /** Sets the reference CD used by subsequent matching requests. */
  void setReferenceCD(ASTCDCompilationUnit cd);

  /** Matches one local variable in its owning Java type and method. */
  Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar);

  /** Matches one formal parameter in its owning Java type and method. */
  Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param);
}
