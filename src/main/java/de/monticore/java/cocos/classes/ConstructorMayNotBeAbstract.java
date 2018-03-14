/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTConstructorDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *
 * @author (last commit) $Author: breuer $
 */
public class ConstructorMayNotBeAbstract implements
    JavaDSLASTConstructorDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1205 A constructor may not be abstract.";

  @Override
  public void check(ASTConstructorDeclaration node) {
    JavaMethodSymbol javaMethodSymbol = (JavaMethodSymbol) node.getSymbol().get();
    if (javaMethodSymbol.isConstructor() && javaMethodSymbol.isAbstract()) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }
}
