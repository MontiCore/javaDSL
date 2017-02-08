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
package de.monticore.java.cocos.interfaces;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class InterfaceNoDuplicateModifier implements JavaDSLASTInterfaceDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public InterfaceNoDuplicateModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.8.3-1
  @Override public void check(ASTInterfaceDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifiers()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modType = typeResolver.getResult()
          .get();
      if (modifiers.contains(modType.getName())) {
        Log.error(
            "0xA0711 modifier '" + modType.getName() + "' is declared more than once in interface '"
                + node
                .getName() + "'.", node.get_SourcePositionStart());
      }
      else {
        modifiers.add(modType.getName());
      }
    }
  }
}