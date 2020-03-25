/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTGreaterThanExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTGreaterThanExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 */
public class GreaterThanOpValid implements CommonExpressionsASTGreaterThanExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public GreaterThanOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTGreaterThanExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0532 each operand of a comparison operator must be of numeric type.",
          node.get_SourcePositionStart());
    }
  }
  
}
