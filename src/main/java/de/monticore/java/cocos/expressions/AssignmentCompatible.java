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

import de.monticore.java.expressions._ast.ASTAssignmentExpression;
import de.monticore.java.expressions._cocos.ExpressionsASTAssignmentExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class AssignmentCompatible implements ExpressionsASTAssignmentExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public AssignmentCompatible(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.26-1, JLS3 15.26-2
  @Override
  public void check(ASTAssignmentExpression node) {
    if (!JavaDSLHelper.isVariable(node.getLeftExpression())) {
      Log.error("0xA0507 first operand of assignment expression must be a variable.",
          node.get_SourcePositionStart());
      return;
    }
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
       Log.error(
          "0xA0509 type '" + "" + "' cannot be converted to type '" + ""
              + "'.",
          node.get_SourcePositionStart());
    }
  }
}
