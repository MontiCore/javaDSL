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
package de.monticore.java.cocos.expressions;

import de.monticore.expressions.mcexpressions._ast.ASTPrimaryThisExpression;
import de.monticore.expressions.mcexpressions._cocos.MCExpressionsASTPrimaryThisExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 07.08.2016.
 */
public class PrimaryThisValid implements MCExpressionsASTPrimaryThisExpressionCoCo {
  
  //JLS3 14.21-1
  @Override
  public void check(ASTPrimaryThisExpression node) {
    String enclosingType = JavaDSLHelper.getEnclosingTypeSymbolName(node);
    JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
        .resolve(enclosingType, JavaTypeSymbol.KIND).get();
    if (typeSymbol.isInterface()) {
      Log.error("0xA0577 keyword 'this' is not allowed in interface.",
          node.get_SourcePositionStart());
    }
  }
  
}

