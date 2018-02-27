/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTShiftExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTShiftExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class ShiftOpValid implements MCExpressionsASTShiftExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ShiftOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.19-1
  @Override
  public void check(ASTShiftExpression node) {
    
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0578 operands of shift operator must have Integral type.",
          node.get_SourcePositionStart());
    }
  }
  
}
