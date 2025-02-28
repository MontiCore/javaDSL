package de.monticore.codeAdaption.matcher.compMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.List;
import java.util.Optional;

/***
 * compose matching Strategy for Method and attributes.
 * allow trying several matching strategies until a matching is found.
 */
public class CompTMemberMatcher implements TMemberMatcher {
  protected TypeMatcher typeMatcher;
  protected List<TMemberMatcher> matchers;

  public CompTMemberMatcher(List<TMemberMatcher> matchers) {
    this.matchers = matchers;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    matchers.forEach(matcher -> matcher.setReferenceCD(cd));
  }

  @Override
  public void setTypeMatcher(TypeMatcher matcher) {
    this.typeMatcher = matcher;
    matchers.forEach(m -> m.setTypeMatcher(matcher));
  }

  @Override
  public TypeMatcher getTypeMatcher() {
    return typeMatcher;
  }

  @Override
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {

    for (TMemberMatcher matcher : matchers) {
      Optional<CodeMatching> matching = matcher.getMatchedMethod(type, method);
      if (matching.isPresent()) {
        return matching;
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration field) {

    for (TMemberMatcher matcher : matchers) {
      Optional<CodeMatching> matching = matcher.getMatchedField(type, field);
      if (matching.isPresent()) {
        return matching;
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    for (TMemberMatcher matcher : matchers) {
      Optional<CodeMatching> matching = matcher.getMatchedSupertype(type, supertype);
      if (matching.isPresent()) {
        return matching;
      }
    }
    return Optional.empty();
  }
}
