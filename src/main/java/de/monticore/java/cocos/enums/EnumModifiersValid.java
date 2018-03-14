/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.enums;

import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTEnumDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class EnumModifiersValid implements JavaDSLASTEnumDeclarationCoCo {
  
  @Override
  public void check(ASTEnumDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaTypeSymbol enumSymbol = (JavaTypeSymbol) node.getSymbol().get();
      // JLS3 8.9-1
      if (enumSymbol.isAbstract()) {
        Log.error("0xA0404 an enum must not be declared 'abstract' at declaration of enum '" + node
            .getName() + "'.", node.get_SourcePositionStart());
      }
      // JLS3 8.9-2
      if (enumSymbol.isFinal()) {
        Log.error(
            "0xA0405 an enum must not be declared 'final' at declaration of enum '" + node.getName()
                + "'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
