/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTBinaryAndOpExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTBinaryAndOpExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 08.06.2016.
 */
public class BinaryAndOpValid implements MCExpressionsASTBinaryAndOpExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public BinaryAndOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.22-1
  @Override
  public void check(ASTBinaryAndOpExpression node) {  
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0511 operands of the bitwise/logical exclusive AND operator must both be of either an integral type or the type boolean.",
          node.get_SourcePositionStart());
    }    
  }
  
}
