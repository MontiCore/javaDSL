/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTLessEqualExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTLessEqualExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * on 08.06.2016.
 */
public class LessEqualOpValid implements CommonExpressionsASTLessEqualExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public LessEqualOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.20.1-1
  @Override
  public void check(ASTLessEqualExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0532 each operand of a comparison operator must be of numeric type.",
          node.get_SourcePositionStart());
    }
  }
  
}
