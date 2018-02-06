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
package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 *
 * @author (last commit) $Author: breuer $
 */
public class AbstractOrNativeMethodHasNoBody implements JavaDSLASTMethodDeclarationCoCo {
  public static final String ERROR_MESSAGE = "0xA1202 An abstract method may not implement a body.";

  @Override
  public void check(ASTMethodDeclaration node) {
    JavaMethodSymbol javaMethodSymbol = (JavaMethodSymbol) node.getSymbol().get();
    if (javaMethodSymbol.isAbstract() && node.isPresentMethodBody()) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }
}
