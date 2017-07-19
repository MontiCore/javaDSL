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
package de.monticore.java.cocos.constructors;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ConstructorMustNamedAsClass implements JavaDSLASTClassDeclarationCoCo {
  
  @Override
  public void check(ASTClassDeclaration node) {
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (JavaMethodSymbol methodSymbol : typeSymbol.getConstructors()) {
      if (!node.getName().equals(methodSymbol.getName())) {
        Log.error(
            "0xA0306 the constructor is not named same as its enclosing class '" + node.getName()
                + "'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
