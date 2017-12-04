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

import de.monticore.assignmentexpressions._ast.ASTIncSuffixExpression;
import de.monticore.assignmentexpressions._cocos.AssignmentExpressionsASTIncSuffixExpressionCoCo;
import de.monticore.assignmentexpressions._ast.ASTDecSuffixExpression;
import de.monticore.assignmentexpressions._cocos.AssignmentExpressionsASTDecSuffixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class SuffixOpValid implements AssignmentExpressionsASTIncSuffixExpressionCoCo,AssignmentExpressionsASTDecSuffixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public SuffixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.14.2-1, JLS3 15.14.2-2
  @Override
  public void check(ASTIncSuffixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0579 the operand expression of suffix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }
    if (!JavaDSLHelper.isVariable(node.getExpression())) {
      Log.error("0xA0580 the operand expression of suffix must be a variable.",
          node.get_SourcePositionStart());
      return;
    }
  }
  
  @Override
  public void check(ASTDecSuffixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0579 the operand expression of suffix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }
    if (!JavaDSLHelper.isVariable(node.getExpression())) {
      Log.error("0xA0580 the operand expression of suffix must be a variable.",
          node.get_SourcePositionStart());
      return;
    }
  }
  
}
