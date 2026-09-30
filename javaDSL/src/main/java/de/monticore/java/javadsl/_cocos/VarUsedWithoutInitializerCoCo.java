package de.monticore.java.javadsl._cocos;

import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTVariableDeclarator;
import de.se_rwth.commons.logging.Log;

public class VarUsedWithoutInitializerCoCo implements JavaDSLASTLocalVariableDeclarationCoCo {
  
  public static final String ERROR_CODE = "0x7A010";
  
  public static final String ERROR_MSG_FORMAT = "  'var' cannot be used without initializer";
  
  @Override
  public void check(ASTLocalVariableDeclaration node) {
    if (node.isVar() && node.sizeVariableDeclarators() > 0) {
      ASTVariableDeclarator variableDeclarator = node.getVariableDeclarator(0);
      if (!variableDeclarator.isPresentVariableInit()) {
        Log.error(ERROR_CODE + ERROR_MSG_FORMAT, node.get_SourcePositionStart());
      }
    }
  }
}
