/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTBooleanAndOpExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTBooleanAndOpExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 08.06.2016.
 */
public class BooleanAndValid implements CommonExpressionsASTBooleanAndOpExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public BooleanAndValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.23-1, JLS3 15.24-1
  @Override
  public void check(ASTBooleanAndOpExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0514 operands of the conditional AND operator must both be of type boolean.",
          node.get_SourcePositionStart());
    }
  }
  
}
