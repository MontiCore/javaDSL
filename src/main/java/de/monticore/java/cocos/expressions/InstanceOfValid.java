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
 * @since TODO
 */
public class InstanceOfValid implements JavaDSLASTExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public InstanceOfValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.20.2-1, JLS3 15.20.2-2, JLS3 15.20.2-3, JLS3 15.20.2-4
  @Override
  public void check(ASTExpression node) {
    if (node.getExpression().isPresent() && node.getInstanceofType().isPresent()) {
      typeResolver.handle(node.getExpression().get());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference typeExp = typeResolver.getResult()
            .get();
        if (JavaDSLHelper.isPrimitiveType(typeExp) && !"null".equals(typeExp.getName())) {
          Log.error(
              "0xA0550 the relational expression operand of the 'instanceof' operator must be a reference type or the null type.",
              node.get_SourcePositionStart());
        }
        node.getInstanceofType().get().accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference typeIns = typeResolver.getResult()
              .get();
          if (JavaDSLHelper.isPrimitiveType(typeIns)) {
            Log.error(
                "0xA0551 the reference type mentioned after the 'instanceof' operator must denote a reference type.",
                node.get_SourcePositionStart());
          }
          else if (!JavaDSLHelper.isReifiableType(typeIns)) {
            Log.error(
                "0xA0552 the reference type mentioned after the 'instanceof' operator must denote a reifiable type.",
                node.get_SourcePositionStart());
          }
          else if (!JavaDSLHelper.castTypeConversionAvailable(typeExp, typeIns)) {
            Log.error(
                "0xA0553 incompatible conditional operand types '" + typeExp.getName() + "', '"
                    + typeIns
                    .getName() + "'.", node.get_SourcePositionStart());
            return;
          }

        }
      }
    }

  }
}
