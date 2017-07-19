/*
 * ******************************************************************************
 * MontiCore Language Workbench
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
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class InterfaceOptionalBoundsAreInterfaces implements JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 4.4-1
  @Override
  public void check(ASTInterfaceDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaTypeSymbol superSymbol : interfaceSymbol.getFormalTypeParameters()) {
        for (int i = 1; i < superSymbol.getSuperTypes().size(); i++) {
          if (node.getEnclosingScope().get()
              .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol superType = (JavaTypeSymbol) node.getEnclosingScope().get()
                .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND).get();
            if (!superType.isInterface()) {
              Log.error(
                  "0xA0714 bound '" + superType.getName() + "' of type-variable '"
                      + superSymbol
                      .getName() + "' must be an interface.", node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
}
