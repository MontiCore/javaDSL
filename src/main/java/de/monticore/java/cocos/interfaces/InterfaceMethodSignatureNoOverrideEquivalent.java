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
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 * Created by Odgrlb on 25.09.2016.
 */
public class InterfaceMethodSignatureNoOverrideEquivalent implements
    JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 8.4.2-1
  @Override public void check(ASTInterfaceDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getSymbol().get();
      List<JavaMethodSymbol> methods = interfaceSymbol.getMethods();
      for (int i = 1; i < methods.size(); i++) {
        for (int j = 0; j < i; j++) {
          JavaMethodSymbol m1 = methods.get(i);
          JavaMethodSymbol m2 = methods.get(j);
          if (JavaDSLHelper.isSubSignature(m1, m2, null) || JavaDSLHelper.isSubSignature(m2, m1, null)) {
            if (JavaDSLHelper.containsParametrizedType(m1) || JavaDSLHelper.containsParametrizedType(m2)) {
              Log.error("0xA0706 erasure of method '" + methods.get(i).getName()
                      + "' is same with another method in interface '" + node.getName() + "'.",
                  node.get_SourcePositionStart());
            }
            else {
              Log.error("0xA0707 method '" + m1.getName() + "' is duplicated in interface '" + node
                  .getName() + "'.", node.get_SourcePositionStart());
            }
            break;
          }
        }
      }
    }
  }
}
