/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.commonexpressions._ast.ASTBooleanNotExpression;
import de.monticore.expressions.commonexpressions._cocos.CommonExpressionsASTBooleanNotExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class BooleanNotValid implements CommonExpressionsASTBooleanNotExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public BooleanNotValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.15.5-1, JLS3 15.15.6-1
  @Override
  public void check(ASTBooleanNotExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0516 operand of the boolean NOT '~' operator must be convertible to primitive integral type.",
          node.get_SourcePositionStart());
    }
  }
  
}
