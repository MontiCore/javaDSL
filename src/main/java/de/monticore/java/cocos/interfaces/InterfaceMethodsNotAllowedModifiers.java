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

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *  on 18.08.2016.
 */
public class InterfaceMethodsNotAllowedModifiers implements
    JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 9.4-2
  @Override
  public void check(ASTInterfaceDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaMethodSymbol methodSymbol : interfaceSymbol.getMethods()) {
        if (methodSymbol.isStrictfp()) {
          Log.error(
              "0xA0708 method '" + methodSymbol.getName() + "' is declared strictfp in interface '"
                  + node
                  .getName() + "'.", node.get_SourcePositionStart());
        }

        if (methodSymbol.isNative()) {
          Log.error(
              "0xA0709 method '" + methodSymbol.getName() + "' is declared native in interface '"
                  + node.getName() + "'.", node.get_SourcePositionStart());
        }

        if (methodSymbol.isSynchronized()) {
          Log.error(
              "0xA0710 method '" + methodSymbol.getName()
                  + "' is declared synchronized in interface '" + node
                  .getName() + "'.", node.get_SourcePositionStart());
        }

      }

    }
  }
}
