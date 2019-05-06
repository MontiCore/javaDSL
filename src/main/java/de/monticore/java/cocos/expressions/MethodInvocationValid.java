/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.commonexpressions._ast.ASTNameExpression;
import de.monticore.expressions.commonexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTCallExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

public class MethodInvocationValid implements CommonExpressionsASTCallExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public MethodInvocationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.12.1-1, JLS3 15.12.1-2, JLS3 15.12.1-3, JLS3 15.12.1-4, JLS3 15.12.1-5
  @Override
  public void check(ASTCallExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      ASTExpression expr = node.getExpression();
      String name = "";
      if (expr instanceof ASTNameExpression) {
        name = ((ASTNameExpression) expr).getName();
      } else if (expr instanceof ASTQualifiedNameExpression) {
        name = ((ASTQualifiedNameExpression) expr).getName();
      }
      Log.error("0xA0574 method '" + name + "' is not found.",
              node.get_SourcePositionStart());
    }
  }
  
}
