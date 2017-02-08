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
package de.monticore.java.cocos.constructors;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class ConstructorsNoDuplicateSignature implements
    JavaDSLASTClassDeclarationCoCo {


  //JLS3 8.8.2-2
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      List<JavaMethodSymbol> constructors = typeSymbol.getConstructors();
      for (int i = 1; i < constructors.size(); i++) {
        for (int j = 0; j < i; j++) {
          JavaMethodSymbol c1 = constructors.get(i);
          JavaMethodSymbol c2 = constructors.get(j);
          if (JavaDSLHelper.isSubSignature(c1, c2, null) || JavaDSLHelper.isSubSignature(c2, c1, null)) {
            if (JavaDSLHelper.containsParametrizedType(c1) || JavaDSLHelper.containsParametrizedType(c2)) {
              Log.error("0xA0312 erasure of constructor '" + constructors.get(i).getName()
                      + "' is same with another constructor in class '" + node.getName() + "'.",
                  node.get_SourcePositionStart());
            }
            else {
              Log.error("0xA0313 constructor '" + c1.getName() + "' is duplicated in class '" + node
                  .getName() + "'.", node.get_SourcePositionStart());
            }
            break;
          }
        }
      }
    }
  }
}

