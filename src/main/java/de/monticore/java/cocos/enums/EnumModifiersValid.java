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
package de.monticore.java.cocos.enums;

import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTEnumDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class EnumModifiersValid implements JavaDSLASTEnumDeclarationCoCo {
  
  @Override
  public void check(ASTEnumDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaTypeSymbol enumSymbol = (JavaTypeSymbol) node.getSymbol().get();
      // JLS3 8.9-1
      if (enumSymbol.isAbstract()) {
        Log.error("0xA0404 an enum must not be declared 'abstract' at declaration of enum '" + node
            .getName() + "'.", node.get_SourcePositionStart());
      }
      // JLS3 8.9-2
      if (enumSymbol.isFinal()) {
        Log.error(
            "0xA0405 an enum must not be declared 'final' at declaration of enum '" + node.getName()
                + "'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
