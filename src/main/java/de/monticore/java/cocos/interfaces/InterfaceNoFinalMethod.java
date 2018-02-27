/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import de.monticore.java.javadsl._ast.ASTInterfaceBodyDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTInterfaceMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class InterfaceNoFinalMethod implements JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 9.4-3
  @Override
  public void check(ASTInterfaceDeclaration node) {
    for (ASTInterfaceBodyDeclaration bodyDeclaration : node.getInterfaceBody()
        .getInterfaceBodyDeclarationList()) {
      if (bodyDeclaration instanceof ASTInterfaceMethodDeclaration) {
        ASTInterfaceMethodDeclaration methodDeclaration = (ASTInterfaceMethodDeclaration) bodyDeclaration;
        if (methodDeclaration.getSymbol().isPresent()) {
          JavaMethodSymbol methodSymbol = (JavaMethodSymbol) methodDeclaration.getSymbol().get();
          if (methodSymbol.isFinal()) {
            Log.error(
                "0xA0712 method '" + methodSymbol.getName() + "' is declared final in interface '"
                    + node
                    .getName() + "'.", node.get_SourcePositionStart());
          }

        }
      }
    }
  }
}
