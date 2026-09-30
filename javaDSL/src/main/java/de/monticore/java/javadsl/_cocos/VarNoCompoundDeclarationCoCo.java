package de.monticore.java.javadsl._cocos;

import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.se_rwth.commons.logging.Log;

public class VarNoCompoundDeclarationCoCo implements JavaDSLASTLocalVariableDeclarationCoCo{
  
  public static final String ERROR_CODE = "0x7A009";
  
  public static final String ERROR_MSG_FORMAT = "  'var' is not allowed in a compound declaration";
  
  @Override
  public void check(ASTLocalVariableDeclaration node) {
    if (node.isVar() && node.sizeVariableDeclarators() > 1) {
      Log.error(ERROR_CODE + ERROR_MSG_FORMAT, node.get_SourcePositionStart());
    }
  }
}
