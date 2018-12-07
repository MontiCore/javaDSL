/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTArrayDimensionByExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.se_rwth.commons.logging.Log;

public class ArrayDimensionByExpressionValid implements JavaDSLASTArrayDimensionByExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ArrayDimensionByExpressionValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.10-1
  @Override
  public void check(ASTArrayDimensionByExpression node) {
    if (node.getExpressionList().isEmpty()) {
      Log.error("an array dimension must be declared.", node.get_SourcePositionStart());
    }
    for (ASTExpression astExpression : node.getExpressionList()) {
      typeResolver.handle(astExpression);
      JavaTypeSymbolReference typeName = typeResolver.getResult().orElse(null);
      if (!typeResolver.getResult().isPresent()
          || !"int".equals(JavaDSLHelper.getUnaryNumericPromotionType(typeName).getName())) {
        Log.error("0xA0505 an array size must be specified by a type promotable to 'int'.",
            node.get_SourcePositionStart());
      }
    }
  }
  
}
