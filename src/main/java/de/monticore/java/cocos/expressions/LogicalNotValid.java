/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.commonexpressions._ast.ASTLogicalNotExpression;
import de.monticore.commonexpressions._cocos.CommonExpressionsASTLogicalNotExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 08.06.2016.
 */
public class LogicalNotValid implements CommonExpressionsASTLogicalNotExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public LogicalNotValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.15.5-1, JLS3 15.15.6-1
  @Override
  public void check(ASTLogicalNotExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0515 operand of the boolean NOT '!' operator must be of type boolean.",
          node.get_SourcePositionStart());
    }
  }
  
}
