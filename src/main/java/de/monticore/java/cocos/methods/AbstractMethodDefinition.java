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
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 23.09.2016.
 */
public class AbstractMethodDefinition implements JavaDSLASTMethodDeclarationCoCo {
  
  @Override
  public void check(ASTMethodDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol().get();
      if (methodSymbol.isAbstract() && node.getEnclosingScope().isPresent()) {
        String name = JavaDSLHelper.getEnclosingTypeSymbolName(node.getEnclosingScope().get());
        if (name != null
            && node.getEnclosingScope().get().resolve(name, JavaTypeSymbol.KIND).isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
              .resolve(name, JavaTypeSymbol.KIND).get();
          if (typeSymbol.isInterface()) {
            return;
          }
          if (typeSymbol.isClass() && !typeSymbol.isAbstract()) {
            Log.error("0xA0801 abstract method '" + methodSymbol.getName()
                + "' must be defined by an abstract class.");
          }
        }
      }
    }
  }
}
