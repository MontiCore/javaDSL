/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTPrefixExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTPrefixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class PrefixOpValid implements MCExpressionsASTPrefixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public PrefixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.15.1-1, JLS3 15.15.2-1, JLS3 15.15.3-1, JLS3 15.15.4-1
  @Override
  public void check(ASTPrefixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0572 the operand expression of prefix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }
    if ("--".equals(node.getPrefixOp()) || "++".equals(node.getPrefixOp())) {
      if (!JavaDSLHelper.isVariable(node.getExpression())) {
        Log.error("0xA0573 the operand expression of prefix must be a variable.",
            node.get_SourcePositionStart());
        return;
      }
    }
  }
  
}
