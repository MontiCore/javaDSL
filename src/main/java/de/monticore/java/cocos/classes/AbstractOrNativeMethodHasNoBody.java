/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *
 * @author (last commit) $Author: breuer $
 */
public class AbstractOrNativeMethodHasNoBody implements JavaDSLASTMethodDeclarationCoCo {
  public static final String ERROR_MESSAGE = "0xA1202 An abstract method may not implement a body.";

  @Override
  public void check(ASTMethodDeclaration node) {
    JavaMethodSymbol javaMethodSymbol = (JavaMethodSymbol) node.getSymbol();
    if (javaMethodSymbol.isAbstract() && node.isPresentMethodBody()) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }
}
