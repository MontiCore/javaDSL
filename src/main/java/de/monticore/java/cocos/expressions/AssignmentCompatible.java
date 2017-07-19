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
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.literals.literals._ast.ASTLiteral;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class AssignmentCompatible implements JavaDSLASTExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public AssignmentCompatible(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.26-1, JLS3 15.26-2
  @Override
  public void check(ASTExpression node) {
    if (node.leftExpressionIsPresent() && node.rightExpressionIsPresent() && node
        .assignmentIsPresent()) {
      if (JavaDSLHelper.rightAndLeftExpressionsValid(node)) {
        if (!JavaDSLHelper.isVariable(node.getLeftExpression().get())) {
          Log.error("0xA0507 first operand of assignment expression must be a variable.",
              node.get_SourcePositionStart());
          return;
        }
        typeResolver.handle(node.getLeftExpression().get());
        JavaTypeSymbolReference leftType = typeResolver.getResult()
            .get();
        if (JavaDSLHelper.isByteType(leftType) || JavaDSLHelper.isCharType(leftType)
            || JavaDSLHelper.isShortType(leftType)) {
          if (node.getRightExpression().get().primaryExpressionIsPresent()) {
            if (node.getRightExpression().get().getPrimaryExpression().get().literalIsPresent()) {
              ASTLiteral literal = node.getRightExpression().get().getPrimaryExpression().get()
                  .getLiteral().get();
              if (literal instanceof ASTIntLiteral) {
                return;
              }
            }
          }
          else if (node.getRightExpression().get().expressionIsPresent()) {
            if (node.getRightExpression().get().getExpression().get()
                .primaryExpressionIsPresent()) {
              if (node.getRightExpression().get().getExpression().get().getPrimaryExpression().get()
                  .literalIsPresent()) {
                ASTLiteral literal = node.getRightExpression().get().getExpression().get()
                    .getPrimaryExpression().get().getLiteral().get();
                if (literal instanceof ASTIntLiteral) {
                  return;
                }
              }
            }
          }
          
        }
        typeResolver.handle(node.getRightExpression().get());
        JavaTypeSymbolReference rightType = typeResolver.getResult()
            .get();
        // JLS3 5.2-1
        if (JavaDSLHelper.safeAssignmentConversionAvailable(rightType, leftType)) {
          return;
        }
        else if (JavaDSLHelper.unsafeAssignmentConversionAvailable(rightType, leftType)) {
          Log.warn(
              "0xA0508 possible unchecked conversion from type '" + rightType.getName() + "' to '"
                  + leftType.getName() + "'.",
              node.get_SourcePositionStart());
        }
        else {
          Log.error(
              "0xA0509 type '" + rightType.getName() + "' cannot be converted to type '" + leftType
                  .getName()
                  + "'.",
              node.get_SourcePositionStart());
        }
        
      }
    }
  }
}
