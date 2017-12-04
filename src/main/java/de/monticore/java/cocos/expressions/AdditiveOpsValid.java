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


import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.commonexpressions._ast.ASTPlusExpression;
import de.monticore.commonexpressions._ast.ASTMinusExpression;
import de.monticore.commonexpressions._cocos.CommonExpressionsASTMinusExpressionCoCo;
import de.monticore.commonexpressions._cocos.CommonExpressionsASTPlusExpressionCoCo;
import de.se_rwth.commons.logging.Log;

/**
 *  on 08.06.2016.
 */
public class AdditiveOpsValid implements CommonExpressionsASTPlusExpressionCoCo, CommonExpressionsASTMinusExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public AdditiveOpsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.18-1, JLS3 15.18-2
  @Override
  public void check(ASTPlusExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0501 types of both operands of the additive operators must be numeric types.",
          node.get_SourcePositionStart());
    }
  }
  
  @Override
  public void check(ASTMinusExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error("0xA0501 types of both operands of the additive operators must be numeric types.",
          node.get_SourcePositionStart());
    }
  }
}
