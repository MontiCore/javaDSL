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
package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class MethodBodyAbsenceAndPresence implements JavaDSLASTMethodDeclarationCoCo {
  
  @Override
  public void check(ASTMethodDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol().get();
      // JLS3 8.4.7-1
      if (methodSymbol.isAbstract() && node.methodBodyIsPresent()) {
        Log.error(
            "0xA0808 abstract method '" + methodSymbol.getName() + "' must not specify a body.");
      }
      // JLS3 8.4.7-1
      if (methodSymbol.isNative() && node.methodBodyIsPresent()) {
        Log.error(
            "0xA0809 native method '" + methodSymbol.getName() + "' must not specify a body.");
      }
      // JLS3 8.4.7-2
      if (!methodSymbol.isAbstract() && !methodSymbol.isNative()
          && !node.getMethodBody().isPresent()) {
        Log.error("0xA0810 method '" + methodSymbol.getName() + "' must specify a body.",
            node.get_SourcePositionStart());
      }
    }
  }
}
