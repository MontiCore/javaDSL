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
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ArrayAccessValid implements JavaDSLASTExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ArrayAccessValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.13-1
  @Override
  public void check(ASTExpression node) {
    if (node.getArrayExpression().isPresent() && node.getIndexExpression().isPresent()) {
      if (!node.getArrayExpression().get().arrayExpressionIsPresent()) {
        typeResolver.handle(node.getArrayExpression().get());
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference type = typeResolver.getResult().get();
          if (type.getDimension() == 0) {
            Log.error("0xA0503 an array required, but '" + type.getName() + "' found.",
                node.get_SourcePositionStart());
          }
        }
      }
      typeResolver.handle(node.getIndexExpression().get());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference typeIndex = typeResolver.getResult()
            .get();
        if (!"int".equals(JavaDSLHelper.getUnaryNumericPromotionType(typeIndex).getName())) {
          Log.error("0xA0502 an array index expression must have a type promotable to 'int'.",
              node.get_SourcePositionStart());
          return;
        }
      }
    }
  }
}
