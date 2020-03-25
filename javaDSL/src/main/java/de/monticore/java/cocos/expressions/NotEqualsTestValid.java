/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTNotEqualsExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTNotEqualsExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 */
public class NotEqualsTestValid implements CommonExpressionsASTNotEqualsExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public NotEqualsTestValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTNotEqualsExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0549 operands of identity test operator have incompatible types.",
          node.get_SourcePositionStart());
    }
  }
  
}
