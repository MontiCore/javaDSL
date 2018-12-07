/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInitializer;
import de.monticore.java.javadsl._ast.ASTVariableInititializerOrExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTVariableDeclaratorCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class ArrayInitializerValid implements JavaDSLASTVariableDeclaratorCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ArrayInitializerValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.10-2
  @Override public void check(ASTVariableDeclarator node) {
    if (node.isPresentVariableInititializerOrExpression() && node.getVariableInititializerOrExpression().isPresentVariableInitializer()) {
      ASTVariableInitializer varNode = node.getVariableInititializerOrExpression().getVariableInitializer();
      if (varNode instanceof ASTArrayInitializer) {
        ASTArrayInitializer arrInitializer = (ASTArrayInitializer) varNode;
        for (ASTVariableInititializerOrExpression var : arrInitializer.getVariableInititializerOrExpressionList()) {
          var.accept(typeResolver);
          JavaTypeSymbolReference extType = typeResolver.getResult()
              .get();
          if (!JavaDSLHelper.isReifiableType(extType)) {
            //10.6.1
            Log.error("0xA0506 an array component type must be a reifiable type.",
                node.get_SourcePositionStart());
            return;
          }
        }
      }
    }
  }
}
