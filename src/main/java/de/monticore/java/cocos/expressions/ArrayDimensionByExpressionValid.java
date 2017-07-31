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

import de.monticore.java.javadsl._ast.ASTArrayDimensionByExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTArrayDimensionByExpressionCoCo;
import de.monticore.expressions.mcexpressions._ast.ASTExpression;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class ArrayDimensionByExpressionValid implements JavaDSLASTArrayDimensionByExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ArrayDimensionByExpressionValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.10-1
  @Override
  public void check(ASTArrayDimensionByExpression node) {
    if (node.getExpressions().isEmpty()) {
      Log.error("an array dimension must be declared.", node.get_SourcePositionStart());
    }
    for (ASTExpression astExpression : node.getExpressions()) {
      typeResolver.handle(astExpression);
      JavaTypeSymbolReference typeName = typeResolver.getResult().orElse(null);
      if (!typeResolver.getResult().isPresent()
          || !"int".equals(JavaDSLHelper.getUnaryNumericPromotionType(typeName).getName())) {
        Log.error("0xA0505 an array size must be specified by a type promotable to 'int'.",
            node.get_SourcePositionStart());
      }
    }
  }
  
}
