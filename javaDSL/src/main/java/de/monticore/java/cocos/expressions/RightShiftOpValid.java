/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.bitexpressions._ast.ASTRightShiftExpression;
import de.monticore.expressions.bitexpressions._cocos.BitExpressionsASTRightShiftExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 */
public class RightShiftOpValid implements BitExpressionsASTRightShiftExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public RightShiftOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTRightShiftExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0578 operands of shift operator must have Integral type.",
          node.get_SourcePositionStart());
    }
  }
}
