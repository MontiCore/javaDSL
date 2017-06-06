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
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 08.06.2016.
 */
public class BooleanNotValid implements JavaDSLASTExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public BooleanNotValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.15.5-1, JLS3 15.15.6-1
  @Override
  public void check(ASTExpression node) {
    if (node.expressionIsPresent() && node.booleanNotIsPresent()) {
      typeResolver.handle(node);
      if ("!".equals(node.getBooleanNot().get()) && !typeResolver.getResult().isPresent()) {
        Log.error("0xA0515 operand of the boolean NOT '!' operator must be of type boolean.",
            node.get_SourcePositionStart());
      }
      if ("~".equals(node.getBooleanNot().get()) && !typeResolver.getResult().isPresent()) {
        Log.error(
            "0xA0516 operand of the boolean NOT '~' operator must be convertible to primitive integral type.",
            node.get_SourcePositionStart());
      }
    }
  }
}