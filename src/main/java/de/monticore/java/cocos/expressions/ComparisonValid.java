/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTComparisonExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTComparisonExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class ComparisonValid implements MCExpressionsASTComparisonExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ComparisonValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.20.1-1
  @Override
  public void check(ASTComparisonExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0532 each operand of a comparison operator must be of numeric type.",
          node.get_SourcePositionStart());
    }
  }
  
}
