/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *  on 18.08.2016.
 */
public class InterfaceMethodsNotAllowedModifiers implements
    JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 9.4-2
  @Override
  public void check(ASTInterfaceDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaMethodSymbol methodSymbol : interfaceSymbol.getMethods()) {
        if (methodSymbol.isStrictfp()) {
          Log.error(
              "0xA0708 method '" + methodSymbol.getName() + "' is declared strictfp in interface '"
                  + node
                  .getName() + "'.", node.get_SourcePositionStart());
        }

        if (methodSymbol.isNative()) {
          Log.error(
              "0xA0709 method '" + methodSymbol.getName() + "' is declared native in interface '"
                  + node.getName() + "'.", node.get_SourcePositionStart());
        }

        if (methodSymbol.isSynchronized()) {
          Log.error(
              "0xA0710 method '" + methodSymbol.getName()
                  + "' is declared synchronized in interface '" + node
                  .getName() + "'.", node.get_SourcePositionStart());
        }

      }

    }
  }
}
