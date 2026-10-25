package de.monticore.codeAdaption.matcher.ignoreMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.Optional;

/**
 * Terminal fallback that accepts unmatched fields, methods, and supertypes unchanged.
 *
 * <p>The ignored matching prevents a validation error and tells update phases not to transform the
 * member. It is installed by {@code IGNORE_NON_MATCHED_TYPE_MEMBER} after all enabled member
 * matching strategies.
 */
public class IgnoreTMemberMatcher implements TMemberMatcher {
  protected ASTCDCompilationUnit cd;
  protected TypeMatcher typeMatcher;

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setTypeMatcher(TypeMatcher matcher) {
    this.typeMatcher = matcher;
  }

  @Override
  public TypeMatcher getTypeMatcher() {
    return typeMatcher;
  }

  @Override
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {
    return Optional.of(new CodeMatching(true));
  }

  @Override
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration field) {
    return Optional.of(new CodeMatching(true));
  }

  @Override
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    return Optional.of(new CodeMatching(true));
  }
}
