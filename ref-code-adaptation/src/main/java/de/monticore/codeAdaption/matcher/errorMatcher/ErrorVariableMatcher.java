package de.monticore.codeAdaption.matcher.errorMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;

/**
 * Terminal fallback that reports unmatched local variables and formal parameters as validation
 * errors. It is installed when {@code IGNORE_NON_MATCHED_VAR} is not enabled.
 */
public class ErrorVariableMatcher implements VariableMatcher {
  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {}

  @Override
  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration locVar) {

    String name = locVar.getVariableDeclarator(0).getDeclarator().getName();
    String pos = AdapterUtils.getPosition(locVar.get_SourcePositionStart());
    Log.error(pos + " No Match found for the Local Variable " + name);
    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedFormalParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter param) {

    String name = param.getDeclarator().getName();
    String pos = AdapterUtils.getPosition(param.get_SourcePositionStart());
    Log.error(pos + " No Match found for the formal parameter " + name);
    return Optional.empty();
  }
}
