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
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

/**
 *
 * @author (last commit) $Author: breuer $
 */
public class SuperClassMayNotBeFinal implements JavaDSLASTClassDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1207 A super class may not be final.";

  @Override
  public void check(ASTClassDeclaration node) {
    Symbol symbol = node.getSymbol().orElse(null);
    if (symbol == null) {
      Log.error("ASTClassDeclaration must have a Symbol.");
    }
    else {
      JavaTypeSymbol javaClassTypeSymbol = (JavaTypeSymbol) symbol;
      if (javaClassTypeSymbol.getSuperClass().isPresent()) {
        final JavaTypeSymbolReference superClass = javaClassTypeSymbol.getSuperClass().get();
        if (superClass.getReferencedSymbol().isFinal()) {
          Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
        }
      }
    }
  }
}
