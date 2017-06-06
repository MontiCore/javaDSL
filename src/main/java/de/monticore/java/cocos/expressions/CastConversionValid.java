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
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class CastConversionValid implements JavaDSLASTExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public CastConversionValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 5.5-0 - JLS3 5.5-11, JLS3 15.16-1
  @Override
  public void check(ASTExpression node) {
    if (JavaDSLHelper.typeCastTypeAndExpressionValid(node)) {
      node.getTypeCastType().get().accept(typeResolver);
      JavaTypeSymbolReference typeCast = typeResolver.getResult()
          .get();
      typeResolver.handle(node.getExpression().get());
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
                + typeCast.getName() + "'.", node.get_SourcePositionStart());
      }

    }
  }
}