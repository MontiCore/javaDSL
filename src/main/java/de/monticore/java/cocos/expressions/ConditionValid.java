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

import de.monticore.java.mcexpressions._ast.ASTConditionalExpression;
import de.monticore.java.mcexpressions._cocos.MCExpressionsASTConditionalExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 19.08.2016.
 */
public class ConditionValid implements MCExpressionsASTConditionalExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ConditionValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.25-1, JLS3 15.25-2
  @Override
  public void check(ASTConditionalExpression node) {
    typeResolver.handle(node.getCondition());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference conditionType = typeResolver.getResult().get();
      if (!JavaDSLHelper.isBooleanType(conditionType)) {
        Log.error("0xA0533 condition expression must have a type boolean.",
            node.get_SourcePositionStart());
      }
      typeResolver.handle(node.getTrueExpression());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference trueType = typeResolver.getResult().get();
        if (JavaDSLHelper.isVoidType(trueType)) {
          Log.error("0xA0534 true expression cannot have a type 'void'.");
        }
        typeResolver.handle(node.getFalseExpression());
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference falseType = typeResolver.getResult().get();
          if (JavaDSLHelper.isVoidType(falseType)) {
            Log.error("0xA0535 false expression cannot have a type 'void'.");
          }
        }
      }
    }
  }
  
}
