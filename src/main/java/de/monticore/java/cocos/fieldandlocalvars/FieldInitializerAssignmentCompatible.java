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
package de.monticore.java.cocos.fieldandlocalvars;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInititializerOrExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.mcexpressions._ast.ASTExpression;
import de.monticore.java.mcexpressions._ast.ASTLiteralExpression;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLArrayInitializerCollector;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTLiteral;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
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
        if (variableDeclarator.getVariableInititializerOrExpression().isPresent()
            && variableDeclarator.getVariableInititializerOrExpression().get() instanceof ASTExpression) {
          ASTExpression astExpression = (ASTExpression) variableDeclarator
              .getVariableInititializerOrExpression().get();
          if (astExpression instanceof ASTLiteralExpression) {
            ASTLiteralExpression primaryExpression =(ASTLiteralExpression) astExpression;
            ASTLiteral literal = primaryExpression.getLiteral();
            if (literal instanceof ASTIntLiteral) {
              return;
            }
          }
        }
      }
      int dim = fieldType.getDimension()+variableDeclarator.getDeclaratorId().getDim().size();
      fieldType.setDimension(dim);
      String expectedArray = "";
      for (int i = 0; i < dim; i++) {
        expectedArray = expectedArray.concat("[]");
      }
      if (variableDeclarator.getVariableInititializerOrExpression().isPresent() && variableDeclarator.getVariableInititializerOrExpression().get().getVariableInitializer().isPresent()
          && variableDeclarator.getVariableInititializerOrExpression().get().getVariableInitializer().get() instanceof ASTArrayInitializer) {
        variableDeclarator.getVariableInititializerOrExpression().get().accept(arrayInitializerCollector);
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
            for (ASTVariableInititializerOrExpression variableInitializer : arrayInitializer
                .getVariableInititializerOrExpressions()) {
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
          // JLS3 5.2.-3 (§15.28)
          else if (JavaDSLHelper.narrowingPrimitiveConversionAvailable(expType, fieldType)) {
            return;
          }
          else {
            Log.error(
                "0xA0603 cannot assign a value of type '" + expType.getName() + "' to '"
                    + fieldType
                    .getName()
                    + "'.",
                    node.get_SourcePositionStart());
          }
        } else {
          Log.error(
              "0xA0614 cannot assign a value to '"
                  + variableDeclarator.getDeclaratorId().getName()
                  + "'.",
                  node.get_SourcePositionStart());
          
        }
      }
    }  
  }
}
