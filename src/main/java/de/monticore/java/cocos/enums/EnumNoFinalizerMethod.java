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
public class EnumNoFinalizerMethod implements JavaDSLASTEnumDeclarationCoCo {

  @Override public void check(ASTEnumDeclaration node) {
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (JavaMethodSymbol methodSymbol : typeSymbol.getMethods()) {
      if ("finalize".equals(methodSymbol.getName())) {
        Log.error(
            "0xA0406 an enum must not declare a 'finalize' method at declaration of enum '" + node
                .getName() + "'.", node.get_SourcePositionStart());
      }
    }
  }
}
