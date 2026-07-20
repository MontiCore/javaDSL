package de.monticore.codeAdaption.matcher.ignoreMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.util.Optional;
import java.util.Set;

/**
 * Terminal fallback that accepts an unmatched Java type without adapting it.
 *
 * <p>The returned ignored matching prevents a validation error, but it does not invent a
 * reference-CD incarnation. Consequently, an unmatched top-level type is not selected for
 * mapping-specific adapted output. This matcher is installed by {@code IGNORE_NON_MATCHED_TYPE}
 * after all enabled matching strategies.
 */
public class IgnoreTypeMatcher implements TypeMatcher {
  protected ASTCDCompilationUnit cd;
  Set<ASTTypeDeclaration> typeDeclarationSet;

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public Set<ASTTypeDeclaration> getAllTypeDeclarations() {
    return typeDeclarationSet;
  }

  @Override
  public void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarationSet) {
    this.typeDeclarationSet = typeDeclarationSet;
  }

  @Override
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type) {
    return Optional.of(new CodeMatching(true));
  }
}
