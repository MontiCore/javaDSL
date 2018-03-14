/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;


import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.mcexpressions._ast.ASTAddExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTAddExpressionCoCo;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class AdditiveOpsValid implements MCExpressionsASTAddExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public AdditiveOpsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.18-1, JLS3 15.18-2
  @Override
  public void check(ASTAddExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0501 types of both operands of the additive operators must be numeric types.",
          node.get_SourcePositionStart());
    }
  }
}
