/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTMultExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTMultExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class MultiplicativeOpsValid implements MCExpressionsASTMultExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public MultiplicativeOpsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.17-1
  @Override
  public void check(ASTMultExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0571 types of both operands of the multiplicative operators must be numeric types.",
          node.get_SourcePositionStart());
    }
  }
  
}
