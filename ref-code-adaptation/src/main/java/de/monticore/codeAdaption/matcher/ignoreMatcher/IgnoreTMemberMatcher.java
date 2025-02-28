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

/***
 *return an empty-matching with the ignore flag set as true.
 *when the previous strategy didn't find machining for attributes or methods.
 *the adaption will ignore the corresponding element.
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
