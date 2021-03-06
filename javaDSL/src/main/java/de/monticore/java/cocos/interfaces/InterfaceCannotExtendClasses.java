/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class InterfaceCannotExtendClasses implements JavaDSLASTInterfaceDeclarationCoCo {

  @Override public void check(ASTInterfaceDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbolReference type : typeSymbol.getInterfaces()) {
        if (node.getEnclosingScope().resolve(type.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeOfSuper = (JavaTypeSymbol) node.getEnclosingScope()
              .resolve(type.getName(), JavaTypeSymbol.KIND).get();
          if (typeOfSuper.isClass()) {
            Log.error("0xA0703 interface '" + node.getName() + "' must extend only interface.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
