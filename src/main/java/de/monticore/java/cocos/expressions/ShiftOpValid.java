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

import de.monticore.shiftexpressions._ast.ASTLeftShiftExpression;
import de.monticore.shiftexpressions._cocos.ShiftExpressionsASTLeftShiftExpressionCoCo;
import de.monticore.shiftexpressions._ast.ASTRightShiftExpression;
import de.monticore.shiftexpressions._cocos.ShiftExpressionsASTRightShiftExpressionCoCo;
import de.monticore.shiftexpressions._ast.ASTLogiaclRightShiftExpression;
import de.monticore.shiftexpressions._cocos.ShiftExpressionsASTLogiaclRightShiftExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * on 08.06.2016.
 */
public class ShiftOpValid implements ShiftExpressionsASTLeftShiftExpressionCoCo,
    ShiftExpressionsASTRightShiftExpressionCoCo,
    ShiftExpressionsASTLogiaclRightShiftExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ShiftOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.19-1
  @Override
  public void check(ASTLeftShiftExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0578 operands of shift operator must have Integral type.",
          node.get_SourcePositionStart());
    }
  }
  
  @Override
  public void check(ASTRightShiftExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0578 operands of shift operator must have Integral type.",
          node.get_SourcePositionStart());
    }
  }
  
  @Override
  public void check(ASTLogiaclRightShiftExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0578 operands of shift operator must have Integral type.",
          node.get_SourcePositionStart());
    }
  }
  
}
