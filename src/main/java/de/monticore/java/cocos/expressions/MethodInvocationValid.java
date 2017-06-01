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
package de.monticore.java.cocos.expressions;

import de.monticore.java.mcexpressions._ast.ASTCallExpression;
import de.monticore.java.mcexpressions._ast.ASTExpression;
import de.monticore.java.mcexpressions._ast.ASTNameExpression;
import de.monticore.java.mcexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.java.mcexpressions._cocos.MCExpressionsASTCallExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

public class MethodInvocationValid implements MCExpressionsASTCallExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public MethodInvocationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.12.1-1, JLS3 15.12.1-2, JLS3 15.12.1-3, JLS3 15.12.1-4, JLS3 15.12.1-5
  @Override
  public void check(ASTCallExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      ASTExpression expr = node.getExpression();
      String name = "";
      if (expr instanceof ASTNameExpression) {
        name = ((ASTNameExpression) expr).getName();
      } else if (expr instanceof ASTQualifiedNameExpression) {
        name = ((ASTQualifiedNameExpression) expr).getName();
      }
      Log.error("0xA0574 method '" + name + "' is not found.",
          node.get_SourcePositionStart());
    }
  }
  
}
