/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.expressions.shiftexpressions._ast.ASTLeftShiftExpression;
import de.monticore.expressions.shiftexpressions._cocos.ShiftExpressionsASTLeftShiftExpressionCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * on 08.06.2016.
 */
public class LeftShiftOpValid implements ShiftExpressionsASTLeftShiftExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public LeftShiftOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.19-1
  @Override
  public void check(ASTLeftShiftExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0578 operands of shift operator must have Integral type.",
          node.get_SourcePositionStart());
    }
  }
  
 
  
  
}
