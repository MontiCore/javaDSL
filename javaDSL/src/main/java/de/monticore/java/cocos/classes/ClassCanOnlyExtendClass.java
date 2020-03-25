/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class ClassCanOnlyExtendClass implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassCanOnlyExtendClass(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSuperClass()) {
      node.getSuperClass().accept(typeResolver);
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (node.isPresentEnclosingScope()) {
        if (node.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
              .resolve(type.getName(), JavaTypeSymbol.KIND).get();
          if (!typeSymbol.isClass()) {
            Log.error(
                "0xA0202 class is not extending class in declaration of '" + node.getName() + "'.",
                node.get_SourcePositionStart());
          }
        }
      }
      else {
        Log.error(
            "0xA0203 super class in class declaration of '" + node.getName() + "' is not found.",
            node.get_SourcePositionStart());
      }
    }
  }
}
