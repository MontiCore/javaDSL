/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTBinaryXorOpExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTBinaryXorOpExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 08.06.2016.
 */
public class BinaryXorOpValid implements MCExpressionsASTBinaryXorOpExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public BinaryXorOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.22-1
  @Override
  public void check(ASTBinaryXorOpExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0512 operands of the bitwise/logical exclusive XOR operator must both be of either an integral type or the type boolean.",
          node.get_SourcePositionStart());
    }
  }
  
}
