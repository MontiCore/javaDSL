package de.monticore.codeAdaption.matcher.infixMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.List;
import java.util.Optional;

/***
 * match an Attributes and method in the reference code to elements
 * in the reference class Diagram by analyzing the infix.
 */
public class InfixTMemberMatcher implements TMemberMatcher {
  protected ASTCDCompilationUnit cd;
  protected TypeMatcher typeMatcher;

  public InfixTMemberMatcher(ASTCDCompilationUnit cd) {
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

  /***
   * match method to a type or an attribute in the reference class diagram.
   * eg:     getId  to id ;
   *     getEntity  to Entity
   */
  @Override
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {

    // resolve references
    List<ISymbol> references = resolveFieldReferencesOf(type, method.getName(), this::match);
    references.addAll(resolveTypeReferencesOf(type, method.getName(), this::match));

    return MatcherHelper.mkMatchingFromInfixRef(references, method.getName());
  }

  /***
   * match field to a type or an attribute in the reference class diagram.
   * eg: entityList to Entity
   *         longId to id
   */
  @Override
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration field) {
    String name = field.getVariableDeclarator(0).getDeclarator().getName();

    // resolve references
    List<ISymbol> references = resolveFieldReferencesOf(type, name, this::match);
    references.addAll(MatcherHelper.resolveReferencesFromType(name, field.getMCType(), cd));

    // check references and build matching
    return MatcherHelper.mkMatchingFromInfixRef(references, name);
  }

  /***
   * match field to a type or an attribute in the reference class diagram.
   * eg: entityList to Entity
   *         longId to id
   */
  @Override
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType superType) {
    String name = JavaLoader.print(superType);

    // resolve references
    List<ISymbol> references = resolveTypeReferencesOf(type, name, this::match);

    // check references and build matching
    return MatcherHelper.mkMatchingFromInfixRef(references, name);
  }

  public boolean match(String element, String infix) {
    return element.toLowerCase().contains(infix.toLowerCase());
  }
}
