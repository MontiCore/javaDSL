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
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class InterfaceInnerTypeNotNamedAsEnclosing implements
    JavaDSLASTInterfaceDeclarationCoCo {

  @Override public void check(ASTInterfaceDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaTypeSymbol innerType : JavaDSLHelper.getAllInnerTypes(typeSymbol)) {
        if (typeSymbol.getName().equals(innerType.getName())) {
          if (innerType.isInterface()) {
            Log.error("0xA0704 inner interface '" + typeSymbol.getName()
                    + "' must not be named same as enclosing interface.",
                node.get_SourcePositionStart());
          }
          if (innerType.isClass()) {
            Log.error("0xA0705 inner class '" + typeSymbol.getName()
                    + "' must not be named same as enclosing interface.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
