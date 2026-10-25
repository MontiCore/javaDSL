package de.monticore.codeAdaption.matcher.infixMatcher;

import static de.monticore.codeAdaption.matcher.MatcherHelper.mkMatchingFromInfixRef;
import static de.monticore.codeAdaption.matcher.MatcherHelper.resolveReferencesFromType;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.symboltable.ISymbol;
import java.util.List;
import java.util.Optional;

/**
 * Matches local variables and formal parameters from reference-CD names embedded in their Java
 * identifier or declared type.
 *
 * <p>For example, {@code Entity currentEntity} may reference the CD type {@code Entity}; the
 * resulting template preserves the surrounding identifier text.
 */
public class InfixVariableMatcher implements VariableMatcher {
  protected ASTCDCompilationUnit cd;

  public InfixVariableMatcher(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar) {
    String varName = locVar.getVariableDeclarator(0).getDeclarator().getName();

    List<ISymbol> references = resolveReferencesFromType(varName, locVar.getMCType(), cd);

    return MatcherHelper.mkMatchingFromInfixRef(references, varName);
  }

  @Override
  public Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param) {
    String varName = param.getDeclarator().getName();

    List<ISymbol> references = resolveReferencesFromType(varName, param.getMCType(), cd);

    return mkMatchingFromInfixRef(references, varName);
  }

  public boolean match(String element, String infix) {
    return element.toLowerCase().contains(infix.toLowerCase());
  }
}
