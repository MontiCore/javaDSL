package de.monticore.codeAdaption.matcher.annotMatcher;

import static de.monticore.codeAdaption.matcher.MatcherHelper.*;

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
 * match an Attributes and method in the reference code to elements
 * in the reference class Diagram form annotations.
 */
public class AnnotTMemberMatcher implements TMemberMatcher {

  protected ASTCDCompilationUnit cd;
  protected TypeMatcher typeMatcher;

  public AnnotTMemberMatcher(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

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
    return getInfoAnnotation(method.getMCModifierList())
        .map(annot -> mkMatchingFromAnnotation(annot, cd));
  }

  @Override
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration field) {
    return getInfoJavaAnnot(field.getJavaModifierList())
        .map(annot -> mkMatchingFromAnnotation(annot, cd));
  }

  @Override
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    return Optional.empty();
  }
}
