/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTIdentityExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTIdentityExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class IdentityTestValid implements MCExpressionsASTIdentityExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public IdentityTestValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.21-2, JLS3 15.21-3-1
  @Override
  public void check(ASTIdentityExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0549 operands of identity test operator have incompatible types.",
          node.get_SourcePositionStart());
    }
  }
  
}
