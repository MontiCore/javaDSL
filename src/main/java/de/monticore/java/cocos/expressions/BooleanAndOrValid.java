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

import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class BooleanAndOrValid implements JavaDSLASTExpressionCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public BooleanAndOrValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.23-1, JLS3 15.24-1
  @Override public void check(ASTExpression node) {
    if (JavaDSLHelper.rightAndLeftExpressionsValid(node)) {
      if (node.booleanOrOpIsPresent()) {
        typeResolver.handle(node);
        if (!typeResolver.getResult().isPresent()) {
          Log.error("0xA0513 operands of the conditional OR operator must both be of type boolean.",
              node.get_SourcePositionStart());
        }
      }
      if (node.booleanAndOpIsPresent()) {
        typeResolver.handle(node);
        if (!typeResolver.getResult().isPresent()) {
          Log.error(
              "0xA0514 operands of the conditional AND operator must both be of type boolean.",
              node.get_SourcePositionStart());
        }
      }
    }

  }
}
