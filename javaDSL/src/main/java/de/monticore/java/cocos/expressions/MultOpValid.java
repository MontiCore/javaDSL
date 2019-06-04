/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTMultExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTMultExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * on 08.06.2016.
 */
public class MultOpValid implements CommonExpressionsASTMultExpressionCoCo    {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public MultOpValid(HCJavaDSLTypeResolver typeResolver) {
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
