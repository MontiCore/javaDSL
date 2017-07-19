/*
 * ******************************************************************************
 * MontiCore Language Workbench
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

import de.monticore.java.javadsl._ast.ASTPrimaryExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTPrimaryExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 07.08.2016.
 */
public class PrimarySuperValid implements JavaDSLASTPrimaryExpressionCoCo {
  
  @Override
  public void check(ASTPrimaryExpression node) {
    if (node.isSuper()) {
      String enclosingType = JavaDSLHelper.getEnclosingTypeSymbolName(node);
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
          .resolve(enclosingType, JavaTypeSymbol.KIND).get();
      if (typeSymbol.isInterface()) {
        Log.error("0xA0575 keyword 'super' is not allowed in interface.",
            node.get_SourcePositionStart());
      }
      if ("Object".equals(typeSymbol.getName())
          || "java.lang.Object".equals(typeSymbol.getName())) {
        Log.error("0xA0576 keyword 'super' is not allowed in class 'Object'.",
            node.get_SourcePositionStart());
        
      }
    }
  }
}
