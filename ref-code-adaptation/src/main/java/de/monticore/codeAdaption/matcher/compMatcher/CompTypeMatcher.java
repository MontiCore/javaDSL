package de.monticore.codeAdaption.matcher.compMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/***
 * compose matching Strategy for Types.
 * allow trying several matching strategies until a matching is found.
 */
public class CompTypeMatcher implements TypeMatcher {
  protected List<TypeMatcher> matchers;
  protected Set<ASTTypeDeclaration> typeDeclarationSet;

  public CompTypeMatcher(List<TypeMatcher> matchers) {
    this.matchers = matchers;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    matchers.forEach(matcher -> matcher.setReferenceCD(cd));
  }

  @Override
  public Set<ASTTypeDeclaration> getAllTypeDeclarations() {
    return typeDeclarationSet;
  }

  @Override
  public void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarationSet) {
    this.typeDeclarationSet = typeDeclarationSet;
    matchers.forEach(typeMatcher -> typeMatcher.setAllTypeDeclarations(typeDeclarationSet));
  }

  @Override
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type) {

    for (TypeMatcher matcher : matchers) {
      Optional<CodeMatching> matching = matcher.getMatchedType(type);
      if (matching.isPresent()) {
        return matching;
      }
    }
    return Optional.empty();
  }
}
