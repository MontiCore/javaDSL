/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.assignmentexpressions._ast.ASTDecSuffixExpression;
import de.monticore.expressions.assignmentexpressions._cocos.AssignmentExpressionsASTDecSuffixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * @author npichler
 */
public class DecSuffixOpValid implements AssignmentExpressionsASTDecSuffixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public DecSuffixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTDecSuffixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0579 the operand expression of suffix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }
    if (!JavaDSLHelper.isVariable(node.getExpression())) {
      Log.error("0xA0580 the operand expression of suffix must be a variable.",
          node.get_SourcePositionStart());
      return;
    }
  }
  
}
