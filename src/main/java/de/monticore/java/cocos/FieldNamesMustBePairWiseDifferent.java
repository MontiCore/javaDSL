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
package de.monticore.java.cocos;

import java.util.HashSet;
import java.util.Set;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 * @since TODO: add version number
 */
public class FieldNamesMustBePairWiseDifferent implements JavaDSLASTTypeDeclarationCoCo {
  public static final String ERROR_MESSAGE = "Field names must be pairwise different.";

  @Override
  public void check(ASTTypeDeclaration node) {
    Set<String> names = new HashSet<String>();
    JavaTypeSymbol javaTypeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (JavaFieldSymbol javaFieldSymbol : javaTypeSymbol.getFields()) {
      String name = javaFieldSymbol.getName();
      if (names.contains(name)) {
        Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
      }
      else {
        names.add(name);
      }
    }
  }
}