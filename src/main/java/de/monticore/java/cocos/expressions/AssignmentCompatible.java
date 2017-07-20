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

import de.monticore.expressions.mcexpressions._ast.ASTAssignmentExpression;
import de.monticore.expressions.mcexpressions._cocos.MCExpressionsASTAssignmentExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class AssignmentCompatible implements MCExpressionsASTAssignmentExpressionCoCo {
  
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
    typeResolver.handle(node.getLeftExpression());
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0538 type of the left side is not defined", node.getLeftExpression().get_SourcePositionStart());
      return;
    }
    JavaTypeSymbolReference leftType = typeResolver.getResult()
        .get();
    
    typeResolver.handle(node.getRightExpression());
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0507 type of the right side is not defined", node.getRightExpression().get_SourcePositionStart());
      return;
    }
    JavaTypeSymbolReference rightType = typeResolver.getResult()
        .get();
    if (JavaDSLHelper.isByteType(leftType) || JavaDSLHelper.isCharType(leftType)
        || JavaDSLHelper.isShortType(leftType)) {
      if (JavaDSLHelper.isIntType(rightType)) {
        return;
      }
    }
    if (JavaDSLHelper.safeAssignmentConversionAvailable(rightType, leftType)) {
      return;
    }
    else if (JavaDSLHelper.unsafeAssignmentConversionAvailable(rightType, leftType)) {
      Log.warn(
          "0xA0508 possible unchecked conversion from type '" + rightType.getName() + "' to '"
              + leftType.getName() + "'.",
          node.get_SourcePositionStart());
    }
    else {
      Log.error(
          "0xA0509 type '" + rightType.getName() + "' cannot be converted to type '" + leftType
              .getName()
              + "'.",
          node.get_SourcePositionStart());
    }
   
  }
}
