package de.monticore.codeAdaption.validator.cocos;

import static de.monticore.codeAdaption.utils.AdapterUtils.getPosition;

import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.statements.mcvardeclarationstatements._cocos.MCVarDeclarationStatementsASTLocalVariableDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * check if there are only one variable for each variable declaration. eg: "String a, b"; is not
 * allowed. allow is "String a; String b";
 */
public class OneVarInDeclaration
    implements JavaDSLASTFieldDeclarationCoCo,
        MCVarDeclarationStatementsASTLocalVariableDeclarationCoCo {
  protected String message =
      "0xRC002 %s invalid variable declaration: expected just one variable per declaration but got more.";

  @Override
  public void check(ASTFieldDeclaration node) {
    if (node.getVariableDeclaratorList().size() > 1) {
      Log.error(String.format(message, getPosition(node.get_SourcePositionStart())));
    }
  }

  @Override
  public void check(ASTLocalVariableDeclaration node) {
    if (node.getVariableDeclaratorList().size() > 1) {
      Log.error(String.format(message, getPosition(node.get_SourcePositionStart())));
    }
  }
}
