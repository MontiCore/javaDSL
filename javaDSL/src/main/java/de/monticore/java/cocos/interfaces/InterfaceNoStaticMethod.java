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
 */
public class InterfaceNoStaticMethod implements JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 9.4-1
  @Override
  public void check(ASTInterfaceDeclaration node) {
    for (ASTInterfaceBodyDeclaration bodyDeclaration : node.getInterfaceBody()
        .getInterfaceBodyDeclarationList()) {
      if (bodyDeclaration instanceof ASTInterfaceMethodDeclaration) {
        ASTInterfaceMethodDeclaration methodDeclaration = (ASTInterfaceMethodDeclaration) bodyDeclaration;
        if (methodDeclaration.isPresentSymbol()) {
          JavaMethodSymbol methodSymbol = (JavaMethodSymbol) methodDeclaration.getSymbol();
          if (methodSymbol.isStatic()) {
            Log.error(
                "0xA0713 method '" + methodSymbol.getName() + "' is declared static in interface '"
                    + node
                    .getName() + "'.", node.get_SourcePositionStart());
          }

        }
      }
    }
  }
}
