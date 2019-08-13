/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.assignmentexpressions._ast.ASTDecPrefixExpression;
import de.monticore.expressions.assignmentexpressions._cocos.AssignmentExpressionsASTDecPrefixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * @author npichler
 */
public class DecPrefixOpValid implements AssignmentExpressionsASTDecPrefixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public DecPrefixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTDecPrefixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0572 the operand expression of prefix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }

    if (!JavaDSLHelper.isVariable(node.getExpression())) {
      Log.error("0xA0573 the operand expression of prefix must be a variable.",
          node.get_SourcePositionStart());
      return;

    }
  }
  
}
