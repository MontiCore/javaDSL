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

import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 08.06.2016.
 */
public class AdditiveOpsValid implements JavaDSLASTExpressionCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public AdditiveOpsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.18-1, JLS3 15.18-2
  @Override
  public void check(ASTExpression node) {
    if (JavaDSLHelper.rightAndLeftExpressionsValid(node) && node.additiveOpIsPresent()) {
      typeResolver.handle(node);
      if (!typeResolver.getResult().isPresent()) {
        Log.error("0xA0501 types of both operands of the additive operators must be numeric types.",
            node.get_SourcePositionStart());
      }
    }
  }
}
