/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.expressions.javaclassexpressions._ast.ASTPrimaryThisExpression;
import de.monticore.expressions.javaclassexpressions._cocos.JavaClassExpressionsASTPrimaryThisExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 07.08.2016.
 */
public class PrimaryThisValid implements JavaClassExpressionsASTPrimaryThisExpressionCoCo {
  
  //JLS3 14.21-1
  @Override
  public void check(ASTPrimaryThisExpression node) {
    String enclosingType = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
        .resolve(enclosingType, JavaTypeSymbol.KIND).get();
    if (typeSymbol.isInterface()) {
      Log.error("0xA0577 keyword 'this' is not allowed in interface.",
          node.get_SourcePositionStart());
    }
  }
  
}

