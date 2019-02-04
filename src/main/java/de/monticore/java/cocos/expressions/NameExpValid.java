/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.javaclassexpressions._ast.ASTNameExpression;
import de.monticore.javaclassexpressions._cocos.JavaClassExpressionsASTNameExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

public class NameExpValid implements JavaClassExpressionsASTNameExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public NameExpValid (HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTNameExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(node.getName() + " could not be found.", node.get_SourcePositionStart());
    }
  }
}
