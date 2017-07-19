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

/**
 *
 * @author (last commit) $Author: breuer $
 * $Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $
 */

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

public class AbstractMethodMayNotBePrivate implements JavaDSLASTMethodDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1200 An abstract method may not be private.";

  @Override
  public void check(ASTMethodDeclaration node) {
    JavaMethodSymbol javaMethodSymbol = (JavaMethodSymbol) node.getSymbol().get();
    if (javaMethodSymbol.isAbstract() && javaMethodSymbol.isPrivate()) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }
}
