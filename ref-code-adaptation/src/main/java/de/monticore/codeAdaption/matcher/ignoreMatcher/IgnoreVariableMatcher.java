package de.monticore.codeAdaption.matcher.ignoreMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.util.Optional;

/***
 *Return an empty-matching with the ignore flag set as true.
 *When the previous strategy didn't find machining for a local-variable or a method-parameter.
 *The adaption will ignore the corresponding element.
 */
public class IgnoreVariableMatcher implements VariableMatcher {
  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {}

  @Override
  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar) {
    return Optional.of(new CodeMatching(true));
  }

  @Override
  public Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param) {
    return Optional.of(new CodeMatching(true));
  }
}
