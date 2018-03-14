/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.enums;

import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTEnumDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class EnumNoFinalizerMethod implements JavaDSLASTEnumDeclarationCoCo {

  @Override public void check(ASTEnumDeclaration node) {
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (JavaMethodSymbol methodSymbol : typeSymbol.getMethods()) {
      if ("finalize".equals(methodSymbol.getName())) {
        Log.error(
            "0xA0406 an enum must not declare a 'finalize' method at declaration of enum '" + node
                .getName() + "'.", node.get_SourcePositionStart());
      }
    }
  }
}
