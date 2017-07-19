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
package de.monticore.java.cocos.fieldandlocalvars;

import de.monticore.java.javadsl._ast.*;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLArrayInitializerCollector;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.literals.literals._ast.ASTLiteral;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class FieldInitializerAssignmentCompatible implements JavaDSLASTFieldDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public FieldInitializerAssignmentCompatible(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTFieldDeclaration node) {
    node.getType().accept(typeResolver);
    if (!typeResolver.getResult().isPresent()) {
      Log.debug("No result in TypeResolver", FieldInitializerAssignmentCompatible.class.getSimpleName());
      return;
    }
    JavaTypeSymbolReference fieldType = typeResolver.getResult()
        .get();
    JavaDSLArrayInitializerCollector arrayInitializerCollector = new JavaDSLArrayInitializerCollector();
    for (ASTVariableDeclarator variableDeclarator : node.getVariableDeclarators()) {
      if (JavaDSLHelper.isByteType(fieldType) || JavaDSLHelper.isCharType(fieldType)
          || JavaDSLHelper.isShortType(fieldType)) {
        if (variableDeclarator.getVariableInitializer().isPresent()
            && variableDeclarator.getVariableInitializer().get() instanceof ASTExpression) {
          ASTExpression astExpression = (ASTExpression) variableDeclarator
              .getVariableInitializer().get();
          if (astExpression.primaryExpressionIsPresent()) {
            if (astExpression.getPrimaryExpression().get().literalIsPresent()) {
              ASTLiteral literal = astExpression.getPrimaryExpression().get().getLiteral()
                  .get();
              if (literal instanceof ASTIntLiteral) {
                return;
              }
            }
          }
          else if (astExpression.expressionIsPresent()) {
            if (astExpression.getExpression().get().primaryExpressionIsPresent()) {
              if (astExpression.getExpression().get().getPrimaryExpression().get()
                  .literalIsPresent()) {
                ASTLiteral literal = astExpression.getExpression().get()
                    .getPrimaryExpression().get().getLiteral().get();
                if (literal instanceof ASTIntLiteral) {
                  return;
                }
              }
            }
          }
        }
      }
      int dim = fieldType.getDimension()+variableDeclarator.getDeclaratorId().getDim().size();
      String expectedArray = "";
      for (int i = 0; i < dim; i++) {
        expectedArray = expectedArray.concat("[]");
      }
      if (variableDeclarator.getVariableInitializer().isPresent()
          && variableDeclarator.getVariableInitializer().get() instanceof ASTArrayInitializer) {
        variableDeclarator.getVariableInitializer().get().accept(arrayInitializerCollector);
        List<ASTArrayInitializer> arrList = arrayInitializerCollector
            .getArrayInitializerList();
        if (dim > 0 && (arrList.isEmpty() || (dim != arrList.size())) ||
            (dim==0 && !arrList.isEmpty())) {
          String initArray = "";
          for (int i = 0; i < arrList.size(); i++) {
            initArray = initArray.concat("[]");
          }             
          Log.error("0xA0601 type mismatch, cannot convert from '"
              + fieldType.getName() + initArray + "' to '" + fieldType.getName() + expectedArray + "'.",
              node.get_SourcePositionStart());
          return;
        }
        if (dim > 0) {
          for (ASTArrayInitializer arrayInitializer : arrList) {
            for (ASTVariableInitializer variableInitializer : arrayInitializer
                .getVariableInitializers()) {
              typeResolver.handle(variableInitializer);
              if (typeResolver.getResult().isPresent()) {
                JavaTypeSymbolReference arrType = JavaDSLHelper.getComponentType(typeResolver.getResult().get());
                if (!JavaDSLHelper.isReifiableType(arrType)) {
                  Log.error("0xA0613 Array component must be a reifiable type.",
                      node.get_SourcePositionStart());
                  return;
                }
                else {
                  JavaTypeSymbolReference componentType = JavaDSLHelper
                      .getComponentType(fieldType);
                  if (JavaDSLHelper.safeAssignmentConversionAvailable(arrType,
                      componentType)) {
                    return;
                  }
                  else if (JavaDSLHelper
                      .unsafeAssignmentConversionAvailable(arrType, componentType)) {
                    Log.warn(
                        "0xA0602 Possible unchecked conversion from type '" + componentType
                        .getName()
                        + "' to '"
                        + fieldType.getName() + "'.",
                        node.get_SourcePositionStart());
                  }
                  else {
                    Log.error(
                        "0xA0603 cannot assign a value of array type '" + arrType.getName()
                        + "' to '"
                        + componentType
                        .getName()
                        + "'.",
                        node.get_SourcePositionStart());
                  }
                }
              }
            }
          }
        }
      } else {
        // typeResolver.handle(variableDeclarator);
        variableDeclarator.accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference expType = typeResolver.getResult()
              .get();
          if (dim != expType.getDimension()) {
            Log.error(
                "0xA0612  type mismatch, cannot convert from '"
                  + expType.getName()  + "' to '" + fieldType.getName() + expectedArray + "'.",
                    node.get_SourcePositionStart());
            return;
          }
          // JLS3 5.2-1
          if (JavaDSLHelper.safeAssignmentConversionAvailable(expType, fieldType)) {
            return;
          }
          // JLS3 4.4-2
          else if (JavaDSLHelper.unsafeAssignmentConversionAvailable(expType, fieldType)) {
            Log.warn(
                "0xA0602 Possible unchecked conversion from type '" + expType.getName()
                + "' to '"
                + fieldType.getName() + "'.",
                node.get_SourcePositionStart());
          }
          else {
            Log.error(
                "0xA0603 cannot assign a value of type '" + expType.getName() + "' to '"
                    + fieldType
                    .getName()
                    + "'.",
                    node.get_SourcePositionStart());
          }
        }
      }
    }  
  }
}
