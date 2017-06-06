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
public class ClassNotStatic implements JavaDSLASTClassDeclarationCoCo {

  @Override
  public void check(ASTClassDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      if (!typeSymbol.getInnerTypes().isEmpty()) {
        for (JavaTypeSymbol innerType : typeSymbol.getInnerTypes()) {
          innerType.setInnerType(true);
        }
      }
      if (!typeSymbol.isInnerType() && typeSymbol.isStatic()) {
        Log.error(
            "0xA0218 a non-member class is not allowed to be static in the declaration of the class '"
                + node.getName() + "'.", node.get_SourcePositionStart());
      }
      //isInnerType is set always false!!
      /*
      if (!typeSymbol.isInnerType() && typeSymbol.isStatic()) {
        Log.error("0xA0210 A non-member Class is not allowed to be static in the declaration of the Class '"
            + node.getName() + "'.", node.get_SourcePositionStart());
      }
      */
    }
  }
}
