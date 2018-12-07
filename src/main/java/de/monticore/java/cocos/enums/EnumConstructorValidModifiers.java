/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.enums;

import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTEnumDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *  on 24.07.2016.
 */
public class EnumConstructorValidModifiers implements JavaDSLASTEnumDeclarationCoCo {

  @Override
  public void check(ASTEnumDeclaration node) {
    JavaTypeSymbol enumSymbol = (JavaTypeSymbol) node.getSymbol();
    for (JavaMethodSymbol methodSymbol : enumSymbol.getConstructors()) {
      if (methodSymbol.isPublic()) {
        Log.error(
            "0xA0401 an enum constructor must not be declared 'public' at declaration of enum '"
                + node.getName() + "'.", node.get_SourcePositionStart());
      }
      if (methodSymbol.isProtected()) {
        Log.error(
            "0xA0402 an enum constructor must not be declared 'protected' at declaration of enum '"
                + node.getName() + "'.", node.get_SourcePositionStart());
      }
    }
  }
}
