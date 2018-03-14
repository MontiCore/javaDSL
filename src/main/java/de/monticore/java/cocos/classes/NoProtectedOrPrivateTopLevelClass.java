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
public class NoProtectedOrPrivateTopLevelClass implements JavaDSLASTClassDeclarationCoCo {

  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol symbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaTypeSymbol typeSymbol : symbol.getInnerTypes()) {
        typeSymbol.setInnerType(true);
      }
      if (!symbol.isInnerType()) {
        if (symbol.isProtected()) {
          Log.error(
              "0xA0222 top-level class '" + node.getName() + "' may not be declared 'protected'.",
              node.get_SourcePositionStart());
        }
        if (symbol.isPrivate()) {
          Log.error(
              "0xA0223 top-level class '" + node.getName() + "' may not be declared 'private'.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
