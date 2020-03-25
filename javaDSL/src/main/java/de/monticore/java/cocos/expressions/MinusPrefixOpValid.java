/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.assignmentexpressions._ast.ASTMinusPrefixExpression;
import de.monticore.expressions.assignmentexpressions._cocos.AssignmentExpressionsASTMinusPrefixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 */
public class MinusPrefixOpValid implements AssignmentExpressionsASTMinusPrefixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public MinusPrefixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTMinusPrefixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0572 the operand expression of prefix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }
  }
}
