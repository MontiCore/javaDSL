/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.mcexpressions._ast.ASTPrimarySuperExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTPrimarySuperExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 07.08.2016.
 */
public class PrimarySuperValid implements MCExpressionsASTPrimarySuperExpressionCoCo {
  
  @Override
  public void check(ASTPrimarySuperExpression node) {
    String enclosingType = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
        .resolve(enclosingType, JavaTypeSymbol.KIND).get();
    if (typeSymbol.isInterface()) {
      Log.error("0xA0575 keyword 'super' is not allowed in interface.",
          node.get_SourcePositionStart());
    }
    if ("Object".equals(typeSymbol.getName())
        || "java.lang.Object".equals(typeSymbol.getName())) {
      Log.error("0xA0576 keyword 'super' is not allowed in class 'Object'.",
          node.get_SourcePositionStart());     
    }
  }
  
}
