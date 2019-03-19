/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.assignmentexpressions._ast.ASTIncSuffixExpression;
import de.monticore.expressions.assignmentexpressions._cocos.AssignmentExpressionsASTIncSuffixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class IncSuffixOpValid implements AssignmentExpressionsASTIncSuffixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public IncSuffixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.14.2-1, JLS3 15.14.2-2
  @Override
  public void check(ASTIncSuffixExpression node) {
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
