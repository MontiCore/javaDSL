/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.types.types._ast.ASTVoidType;
import de.se_rwth.commons.logging.Log;

/**
 * Created by MB
 */
public class MethodReturnVoid implements JavaDSLASTMethodDeclarationCoCo {
      
  @Override
  public void check(ASTMethodDeclaration node) {
    if (node.getMethodSignature().getReturnType() instanceof ASTVoidType
        && node.getMethodSignature().getDimList().size() > 0) {
      Log.error("0xA1208 Invalid return type for '" + node.getMethodSignature().getName()
          + "'. The void type must have dimension 0.",
          node.get_SourcePositionStart());
    }
  }
}
