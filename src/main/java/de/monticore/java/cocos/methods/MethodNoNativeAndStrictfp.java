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
public class MethodNoNativeAndStrictfp implements JavaDSLASTMethodDeclarationCoCo {

  //JLS3 8.4.3-4
  @Override public void check(ASTMethodDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol().get();
      if (methodSymbol.isNative() && methodSymbol.isStrictfp()) {
        Log.error("0xA0819 method '" + node.getMethodSignature().getName()
                + "' must not be both 'native' and 'strictfp'.",
            node.get_SourcePositionStart());
      }
    }

  }
}
