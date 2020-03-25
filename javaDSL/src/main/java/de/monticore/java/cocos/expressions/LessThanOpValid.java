/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTLessThanExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTLessThanExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 */
public class LessThanOpValid implements CommonExpressionsASTLessThanExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public LessThanOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTLessThanExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0532 each operand of a comparison operator must be of numeric type.",
          node.get_SourcePositionStart());
    }
  }
}
