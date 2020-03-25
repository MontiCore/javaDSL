/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *
 */
public class AbstractMethodMayNotBeStatic implements JavaDSLASTMethodDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1201 An abstract method may not be static.";

  @Override
  public void check(ASTMethodDeclaration node) {
    JavaMethodSymbol javaMethodSymbol = (JavaMethodSymbol) node.getSymbol();
    if (javaMethodSymbol.isAbstract() && javaMethodSymbol.isStatic()) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }
}
