/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

/**
 *
 * @author (last commit) $Author: breuer $
 * $Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $
 */

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

public class AbstractMethodMayNotBePrivate implements JavaDSLASTMethodDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1200 An abstract method may not be private.";

  @Override
  public void check(ASTMethodDeclaration node) {
    JavaMethodSymbol javaMethodSymbol = (JavaMethodSymbol) node.getSymbol().get();
    if (javaMethodSymbol.isAbstract() && javaMethodSymbol.isPrivate()) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }
}
