/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
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
public class InterfaceNoStaticMethod implements JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 9.4-1
  @Override
  public void check(ASTInterfaceDeclaration node) {
    for (ASTInterfaceBodyDeclaration bodyDeclaration : node.getInterfaceBody()
        .getInterfaceBodyDeclarations()) {
      if (bodyDeclaration instanceof ASTInterfaceMethodDeclaration) {
        ASTInterfaceMethodDeclaration methodDeclaration = (ASTInterfaceMethodDeclaration) bodyDeclaration;
        if (methodDeclaration.getSymbol().isPresent()) {
          JavaMethodSymbol methodSymbol = (JavaMethodSymbol) methodDeclaration.getSymbol().get();
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
