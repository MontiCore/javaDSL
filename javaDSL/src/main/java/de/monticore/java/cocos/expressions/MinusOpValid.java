/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTMinusExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTMinusExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * @author npichler
 */
public class MinusOpValid implements CommonExpressionsASTMinusExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public MinusOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTMinusExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0501 types of both operands of the additive operators must be numeric types.",
          node.get_SourcePositionStart());
    }
  }
}
