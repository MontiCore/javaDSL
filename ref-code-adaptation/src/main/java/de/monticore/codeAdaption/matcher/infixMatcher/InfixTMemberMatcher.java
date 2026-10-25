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

/**
 * Matches handwritten fields, methods, and declared supertypes by reference-CD names embedded in
 * their Java names or types.
 *
 * <p>For example, {@code getEntity()} may reference type {@code Entity}, while {@code getId()} may
 * reference attribute {@code id}. Method candidates are additionally restricted by parameter
 * count so overloads with different arity do not all become references.
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

  /** Matches a method name to embedded reference fields, types, and same-arity methods. */
  @Override
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {

    // Get parameter count from the Java method for overloaded method matching
    int paramCount = getMethodParameterCount(method);

    // resolve references with parameter count filtering for overloaded methods
    List<ISymbol> references = resolveFieldReferencesOf(type, method.getName(), this::match);
    references.addAll(resolveTypeReferencesOf(type, method.getName(), this::match));
    references.addAll(resolveMethodReferencesOf(type, method.getName(), this::match, paramCount));

    return MatcherHelper.mkMatchingFromInfixRef(references, method.getName());
  }

  private int getMethodParameterCount(ASTMethodDeclaration method) {
    if (!method.getFormalParameters().isPresentFormalParameterListing()) {
      return 0;
    }
    return method.getFormalParameters().getFormalParameterListing().getFormalParameterList().size();
  }

  /** Matches a field through reference names embedded in its name and declared type. */
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

  /** Matches a declared Java supertype to embedded reference-CD type names. */
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
