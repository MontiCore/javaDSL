/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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
package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class ClassOptionalBoundsAreInterfaces implements JavaDSLASTClassDeclarationCoCo {

  //JLS3 4.4-1
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaTypeSymbol superSymbol : classSymbol.getFormalTypeParameters()) {
        for (int i = 1; i < superSymbol.getSuperTypes().size(); i++) {
          if (node.getEnclosingScope().get()
              .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol superType = (JavaTypeSymbol) node.getEnclosingScope().get()
                .resolve(superSymbol.getSuperTypes().get(i).getName(), JavaTypeSymbol.KIND).get();
            if (!superType.isInterface()) {
              Log.error(
                  "0xA0219 bound '" + superType.getName() + "' of type-variable '"
                      + superSymbol
                      .getName() + "' must be an interface.", node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
}
