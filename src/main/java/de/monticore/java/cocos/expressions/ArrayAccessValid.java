/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.expressions.shiftexpressions._ast.ASTArrayExpression;
import de.monticore.expressions.shiftexpressions._cocos.ShiftExpressionsASTArrayExpressionCoCo;
import de.se_rwth.commons.logging.Log;

public class ArrayAccessValid implements ShiftExpressionsASTArrayExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ArrayAccessValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.13-1
  @Override
  public void check(ASTArrayExpression node) {
    typeResolver.handle(node.getExpression());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (type.getDimension() == 0) {
        Log.error("0xA0503 an array required, but '" + type.getName() + "' found.",
            node.get_SourcePositionStart());
      }
    }
    
    typeResolver.handle(node.getIndexExpression());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typeIndex = typeResolver.getResult()
          .get();
      if (!"int".equals(JavaDSLHelper.getUnaryNumericPromotionType(typeIndex).getName())) {
        Log.error("0xA0502 an array index expression must have a type promotable to 'int'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
