package de.monticore.codeAdaption.matcher.compMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import java.util.List;
import java.util.Optional;

/***
 * compose matching Strategy for functions parameters and local variables.
 * allow trying several matching strategies until a matching is found.
 */
public class CompVariableMatcher implements VariableMatcher {

  protected List<VariableMatcher> matchers;

  public CompVariableMatcher(List<VariableMatcher> matchers) {
    this.matchers = matchers;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    matchers.forEach(matcher -> matcher.setReferenceCD(cd));
  }

  @Override
  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar) {

    for (VariableMatcher matcher : matchers) {
      Optional<CodeMatching> matching = matcher.getMatchedLocalVariable(type, method, locVar);
      if (matching.isPresent()) {
        return matching;
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param) {

    for (VariableMatcher matcher : matchers) {
      Optional<CodeMatching> matching = matcher.getMatchedFormalParameter(type, method, param);
      if (matching.isPresent()) {
        return matching;
      }
    }
    return Optional.empty();
  }
}
