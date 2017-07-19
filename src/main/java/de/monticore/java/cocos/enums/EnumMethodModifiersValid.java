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
package de.monticore.java.cocos.enums;

import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTEnumDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class EnumMethodModifiersValid implements JavaDSLASTEnumDeclarationCoCo {
  
  @Override
  public void check(ASTEnumDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaTypeSymbol enumSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaMethodSymbol methodSymbol : enumSymbol.getMethods()) {
        if (methodSymbol.isAbstract()) {
          Log.error(
              "0xA0403 an enum must not contain abstract in enum declaration '" + node.getName()
                  + "'.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
