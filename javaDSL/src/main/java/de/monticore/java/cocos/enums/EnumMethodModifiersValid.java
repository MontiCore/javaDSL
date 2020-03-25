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
 */
public class EnumMethodModifiersValid implements JavaDSLASTEnumDeclarationCoCo {
  
  @Override
  public void check(ASTEnumDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol enumSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaMethodSymbol methodSymbol : enumSymbol.getMethods()) {
        if (methodSymbol.isAbstract()) {
          Log.error(
              "0xA0403 an enum must not contain abstract in enum declaration '" + node.getName()
                  + "'.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
