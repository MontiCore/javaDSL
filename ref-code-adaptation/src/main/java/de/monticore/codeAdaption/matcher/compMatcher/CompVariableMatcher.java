package de.monticore.codeAdaption.matcher.compMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import java.util.List;
import java.util.Optional;

/**
 * Applies formal-parameter and local-variable matchers in configured precedence order and returns
 * the first result. Annotation and infix strategies run before the terminal ignore-or-error policy.
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
