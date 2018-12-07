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

import de.monticore.assignmentexpressions._ast.ASTDecPrefixExpression;
import de.monticore.assignmentexpressions._cocos.AssignmentExpressionsASTDecPrefixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * @author npichler
 */
public class DecPrefixOpValid implements AssignmentExpressionsASTDecPrefixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public DecPrefixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTDecPrefixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0572 the operand expression of prefix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }

    if (!JavaDSLHelper.isVariable(node.getExpression())) {
      Log.error("0xA0573 the operand expression of prefix must be a variable.",
          node.get_SourcePositionStart());
      return;

    }
  }
  
}
