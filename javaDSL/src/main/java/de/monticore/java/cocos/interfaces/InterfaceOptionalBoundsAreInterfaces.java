/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class InterfaceOptionalBoundsAreInterfaces implements JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 4.4-1
  @Override
  public void check(ASTInterfaceDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbol superSymbol : interfaceSymbol.getFormalTypeParameters()) {
        for (int i = 1; i < superSymbol.getSuperTypes().size(); i++) {
          if (node.getEnclosingScope()
              .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol superType = (JavaTypeSymbol) node.getEnclosingScope()
                .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND).get();
            if (!superType.isInterface()) {
              Log.error(
                  "0xA0714 bound '" + superType.getName() + "' of type-variable '"
                      + superSymbol
                      .getName() + "' must be an interface.", node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
}
