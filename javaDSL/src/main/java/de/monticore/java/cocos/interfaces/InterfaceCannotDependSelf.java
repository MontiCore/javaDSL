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
public class InterfaceCannotDependSelf implements JavaDSLASTInterfaceDeclarationCoCo {

  @Override public void check(ASTInterfaceDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbolReference superInterface : typeSymbol.getInterfaces()) {
        if (superInterface.getName().equals(typeSymbol.getName())) {
          Log.error("0xA0702 interface '" + node.getName() + "' must not extend itself.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
