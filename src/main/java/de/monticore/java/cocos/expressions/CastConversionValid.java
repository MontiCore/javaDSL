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

import de.monticore.java.expressions._ast.ASTTypeCastExpression;
import de.monticore.java.expressions._cocos.ExpressionsASTTypeCastExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class CastConversionValid implements ExpressionsASTTypeCastExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public CastConversionValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  // JLS3 5.5-0 - JLS3 5.5-11, JLS3 15.16-1
  @Override
  public void check(ASTTypeCastExpression node) {
    
    node.getTypeCastType().accept(typeResolver);
    JavaTypeSymbolReference typeCast = typeResolver.getResult()
        .get();
    typeResolver.handle(node.getExpression());
    JavaTypeSymbolReference typeExp = typeResolver.getResult()
        .get();
    if (JavaDSLHelper.safeCastTypeConversionAvailable(typeExp, typeCast)) {
      return;
    }
    else if (JavaDSLHelper.unsafeCastTypeConversionAvailable(typeExp, typeCast)) {
      Log.warn("0xA0517 possible unchecked cast conversion from '" + typeExp.getName() + "' to '"
          + typeCast.getName()
          + "'.", node.get_SourcePositionStart());
    }
    else {
      Log.error(
          "0xA0518 cannot cast an expression of type '" + typeExp.getName()
              + "' to the target type '"
              + typeCast.getName() + "'.",
          node.get_SourcePositionStart());
    }
    
  }
}