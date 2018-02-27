/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.constructors;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ConstructorMustNamedAsClass implements JavaDSLASTClassDeclarationCoCo {
  
  @Override
  public void check(ASTClassDeclaration node) {
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (JavaMethodSymbol methodSymbol : typeSymbol.getConstructors()) {
      if (!node.getName().equals(methodSymbol.getName())) {
        Log.error(
            "0xA0306 the constructor is not named same as its enclosing class '" + node.getName()
                + "'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
