/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTDivideExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTDivideExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 */
public class DivideOpValid implements CommonExpressionsASTDivideExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public DivideOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTDivideExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0571 types of both operands of the multiplicative operators must be numeric types.",
          node.get_SourcePositionStart());
    }
  }
  
}
