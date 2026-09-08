package de.monticore.codeAdaption.matcher.compMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Applies type-matching strategies in configured precedence order and returns the first result.
 *
 * <p>A present ignored matching is still a result and stops the chain. The validator therefore
 * installs annotation/name/infix strategies first and an ignore-or-error policy last.
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
