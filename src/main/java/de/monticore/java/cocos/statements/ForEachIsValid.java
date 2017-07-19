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
package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTEnhancedForControl;
import de.monticore.java.javadsl._ast.ASTForStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTForStatementCoCo;
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
public class ForEachIsValid implements JavaDSLASTForStatementCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public ForEachIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.14.2-1
  @Override public void check(ASTForStatement node) {
    if (node.getForControl() instanceof ASTEnhancedForControl) {
      ASTEnhancedForControl forControl = (ASTEnhancedForControl) node.getForControl();
      typeResolver.handle(forControl.getFormalParameter());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference formalType = typeResolver.getResult()
            .get();
        typeResolver.handle(forControl.getExpression());
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference expressionType = typeResolver
              .getResult().get();
          boolean isIterable = JavaDSLHelper.isIterableType(expressionType);
          if (expressionType.getDimension() == 0 && !isIterable) {
            Log.error(
                "0xA0907 expression of the for-each statement must be an array or Iterable.",
                node.get_SourcePositionStart());
          }
          else if (expressionType.getDimension() > 0) {
            if (!JavaDSLHelper.assignmentConversionAvailable(
                new JavaTypeSymbolReference(expressionType.getName(),
                    expressionType.getEnclosingScope(), 0), formalType)) {
              Log.error(
                  "0xA0908 variable in the for-each statement is not compatible with the expression, got : "
                      + formalType.getName() + ", expected : " + expressionType.getName() + ".",
                  node.get_SourcePositionStart());
            }
          }
          else if (isIterable) {
            if (expressionType.getActualTypeArguments().isEmpty()) {
              if (!JavaDSLHelper.assignmentConversionAvailable(
                  new JavaTypeSymbolReference(expressionType.getName(),
                      expressionType.getEnclosingScope(), 0), formalType)) {
                Log.error(
                    "0xA0908 variable in the for-each statement is not compatible with the expression, got : "
                        + formalType.getName() + ", expected : " + expressionType.getName() + ".",
                    node.get_SourcePositionStart());
              }
            }
            else if (!JavaDSLHelper.assignmentConversionAvailable(
                (JavaTypeSymbolReference) expressionType.getActualTypeArguments().get(0).getType(),
                formalType)) {
              Log.error(
                  "0xA0908 variable in the for-each statement is not compatible with the expression, got : "
                      + formalType.getName() + ", expected : " + expressionType
                      .getActualTypeArguments().get(0).getType().getName() + ".",
                  node.get_SourcePositionStart());
            }
          }

        }
      }
    }
  }
}
