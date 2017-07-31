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

import de.monticore.expressions.mcexpressions._ast.ASTPrefixExpression;
import de.monticore.expressions.mcexpressions._cocos.MCExpressionsASTPrefixExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class PrefixOpValid implements MCExpressionsASTPrefixExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public PrefixOpValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.15.1-1, JLS3 15.15.2-1, JLS3 15.15.3-1, JLS3 15.15.4-1
  @Override
  public void check(ASTPrefixExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(
          "0xA0572 the operand expression of prefix operator must have type convertible to numeric type.",
          node.get_SourcePositionStart());
    }
    if ("--".equals(node.getPrefixOp().get()) || "++".equals(node.getPrefixOp().get())) {
      if (!JavaDSLHelper.isVariable(node.getExpression())) {
        Log.error("0xA0573 the operand expression of prefix must be a variable.",
            node.get_SourcePositionStart());
        return;
      }
    }
  }
  
}
