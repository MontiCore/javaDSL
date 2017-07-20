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
package de.monticore.java.cocos;

import java.util.Collection;
import java.util.Map.Entry;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @since TODO: add version number
 */
public class NestedTypeMayNotHaveSameNameAsEnclosingType implements JavaDSLASTTypeDeclarationCoCo {

  public static final String ERROR_MESSAGE = "A nested type may not have the same name as its enclosing class";

  @Override
  public void check(ASTTypeDeclaration node) {
    JavaTypeSymbol javaTypeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (Entry<String, Collection<Symbol>> entry : javaTypeSymbol.getSpannedScope()
        .getLocalSymbols().entrySet()) {
      for (Symbol nestedSymbol : entry.getValue()) {
        if (nestedSymbol instanceof JavaTypeSymbol) {
          JavaTypeSymbol nestedTypeSymbol = (JavaTypeSymbol) nestedSymbol;
          if (nestedTypeSymbol.getName().equals(javaTypeSymbol.getName())) {
            Log.error(ERROR_MESSAGE, nestedTypeSymbol.getAstNode().get().get_SourcePositionStart());
          }
        }
      }
    }
  }
}
