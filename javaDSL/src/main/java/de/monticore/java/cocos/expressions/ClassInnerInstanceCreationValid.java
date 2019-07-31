/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.javadsl._ast.ASTInnerCreatorExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTInnerCreatorExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 04.08.2016.
 */
public class ClassInnerInstanceCreationValid implements JavaDSLASTInnerCreatorExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassInnerInstanceCreationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTInnerCreatorExpression node) {
    typeResolver.handle(node.getExpression());
    if (typeResolver.getResult().isPresent()) {
      if (JavaDSLHelper.isPrimitiveType(typeResolver.getResult().get())) {
        Log.error("0xA0519 the class must be a reference type");
      }
      if (node.getInnerCreator().getName().contains(".")) {
        Log.error("0xA0520 Inner class must have a simple name.");
      }
      JavaTypeSymbolReference primaryType = typeResolver.getResult().get();
      if (node.getEnclosingScope().resolve(primaryType.getName(), JavaTypeSymbol.KIND)
          .isPresent()) {
        JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
            .resolve(primaryType.getName(), JavaTypeSymbol.KIND).get();
        if (typeSymbol.getSpannedScope()
            .resolveMany(node.getInnerCreator().getName(), JavaTypeSymbol.KIND).size() > 1) {
          Log.error("0xA0521 inner class '" + node.getInnerCreator().getName()
              + "' is ambiguous.", node.get_SourcePositionStart());
        }
        else if (typeSymbol.getSpannedScope()
            .resolveMany(node.getInnerCreator().getName(), JavaTypeSymbol.KIND).isEmpty()) {
          Log.error("0xA0522 inner class '" + node.getInnerCreator().getName()
              + "' does not exist in class '" + typeSymbol.getName() + "'.",
              node.get_SourcePositionStart());
        }
        else if (typeSymbol.getSpannedScope()
            .resolve(node.getInnerCreator().getName(), JavaTypeSymbol.KIND).isPresent()) {
          JavaTypeSymbol innerType = (JavaTypeSymbol) typeSymbol.getSpannedScope()
              .resolve(node.getInnerCreator().getName(), JavaTypeSymbol.KIND).get();
          // JLS3 15.9.1-2
          if (node.getInnerCreator().getClassCreatorRest().isPresentClassBody()) {
            if (innerType.isFinal()) {
              Log.error("0xA0523 cannot create an instance of final inner class.",
                  node.get_SourcePositionStart());
            }
            if (innerType.isEnum()) {
              Log.error("0xA0524 cannot create an instance of inner enum class.",
                  node.get_SourcePositionStart());
            }
          }
          else {
            // JLS3 15.9.1-4
            if (innerType.isAbstract()) {
              Log.error("0xA0525 cannot create an instance of inner abstract class.",
                  node.get_SourcePositionStart());
            }
            if (innerType.isEnum()) {
              Log.error("0xA0526 cannot create an instance of inner enum class.",
                  node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
  
}
