package de.monticore.codeAdaption.matcher.annotMatcher;

import static de.monticore.codeAdaption.matcher.MatcherHelper.*;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.util.Optional;

/***
 * match a methods parameters and local variables in the reference code to elements
 * in the reference class Diagram form annotations.
 */
public class AnnotVariableMatcher implements VariableMatcher {
  protected ASTCDCompilationUnit cd;

  public AnnotVariableMatcher(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {}

  @Override
  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar) {
    return getInfoAnnotation(locVar.getMCModifierList())
        .map(annot -> mkMatchingFromAnnotation(annot, cd));
  }

  @Override
  public Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param) {
    return getInfoJavaAnnot(param.getJavaModifierList())
        .map(annot -> mkMatchingFromAnnotation(annot, cd));
  }
}
