/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ClassOptionalBoundsAreInterfaces implements JavaDSLASTClassDeclarationCoCo {

  //JLS3 4.4-1
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbol superSymbol : classSymbol.getFormalTypeParameters()) {
        for (int i = 1; i < superSymbol.getSuperTypes().size(); i++) {
          if (node.getEnclosingScope()
              .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol superType = (JavaTypeSymbol) node.getEnclosingScope()
                .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND).get();
            if (!superType.isInterface()) {
              Log.error(
                  "0xA0219 bound '" + superType.getName() + "' of type-variable '"
                      + superSymbol
                      .getName() + "' must be an interface.", node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
}
