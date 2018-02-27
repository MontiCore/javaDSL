/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.javadsl._ast.ASTAnonymousClass;
import de.monticore.java.javadsl._ast.ASTCreatorExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTCreatorExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.se_rwth.commons.logging.Log;

/**
 *  on 26.07.2016.
 */
public class ClassInstanceCreationValid implements JavaDSLASTCreatorExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassInstanceCreationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTCreatorExpression node) {
    if (node.getCreator() instanceof ASTAnonymousClass) {
      node.getCreator().accept(typeResolver);
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference creatorType = typeResolver.getResult().get();
        for (ActualTypeArgument actualTypeArgument : creatorType.getActualTypeArguments()) {
          if ("ASTWildcardType".equals(actualTypeArgument.getType().getName())) {
            // JLS3 14.21-1
            Log.error("0xA0527 a class cannot be instantiated with a wildcard type argument.",
                node.get_SourcePositionStart());
          }
        }
        ASTAnonymousClass ast = (ASTAnonymousClass) node.getCreator();
        if (creatorType.getEnclosingScope().resolve(creatorType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) creatorType.getEnclosingScope()
              .resolve(creatorType.getName(), JavaTypeSymbol.KIND).get();
          // JLS3 15.9.1-1
          if (ast.getClassCreatorRest().isPresentClassBody()) {
            // Anonymous Class
            if (typeSymbol.isFinal()) {
              Log.error("0xA0528 cannot create an instance of final class.",
                  node.get_SourcePositionStart());
            }
            if (typeSymbol.isEnum()) {
              Log.error("0xA0529 cannot create an instance of enum class.",
                  node.get_SourcePositionStart());
            }
            
          }
          else {
            // JLS3 15.9.1-3
            if (typeSymbol.isEnum()) {
              Log.error("0xA0530 cannot create an instance of enum class.",
                  node.get_SourcePositionStart());
            }
            if (typeSymbol.isAbstract()) {
              Log.error("0xA0531 cannot create an instance of abstract class.",
                  node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
}
