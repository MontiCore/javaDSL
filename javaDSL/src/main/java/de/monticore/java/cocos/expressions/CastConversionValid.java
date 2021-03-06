/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import java.util.Optional;

import de.monticore.expressions.javaclassexpressions._ast.ASTTypeCastExpression;
import de.monticore.expressions.javaclassexpressions._cocos.JavaClassExpressionsASTTypeCastExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class CastConversionValid implements JavaClassExpressionsASTTypeCastExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public CastConversionValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 5.5-0 - JLS3 5.5-11, JLS3 15.16-1
  @Override
  public void check(ASTTypeCastExpression node) {
    
    node.getExtType().accept(typeResolver);
    Optional<JavaTypeSymbolReference> typeCast = typeResolver.getResult();
    typeResolver.handle(node.getExpression());
    Optional<JavaTypeSymbolReference> typeExp = typeResolver.getResult();
    if (typeCast.isPresent() && typeExp.isPresent()) {
      if (JavaDSLHelper.safeCastTypeConversionAvailable(typeExp.get(), typeCast.get())) {
        return;
      }
      else if (JavaDSLHelper.unsafeCastTypeConversionAvailable(typeExp.get(), typeCast.get())) {
        Log.warn(
            "0xA0517 possible unchecked cast conversion from '" + typeExp.get().getName() + "' to '"
                + typeCast.get().getName()
                + "'.",
            node.get_SourcePositionStart());
        return;
      }
    }
    String typeName = typeCast.isPresent()?typeCast.get().getName():"";
    String expName = typeExp.isPresent()?typeExp.get().getName():"";
    Log.error(
        "0xA0518 cannot cast an expression of type '" + expName
            + "' to the target type '"
            + typeName + "'.",
        node.get_SourcePositionStart());
    
  }
}
