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
import de.monticore.java.javadsl._cocos.JavaDSLASTLocalVariableDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLArrayInitializerCollector;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.literals.literals._ast.ASTLiteral;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**

 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO * TODO
 */
public class LocalVariableInitializerAssignmentCompatible implements
    JavaDSLASTLocalVariableDeclarationCoCo {
    
  HCJavaDSLTypeResolver typeResolver;
  
  public LocalVariableInitializerAssignmentCompatible(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTLocalVariableDeclaration node) {
    node.getType().accept(typeResolver);
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference localVarType = typeResolver.getResult()
          .get();
      JavaDSLArrayInitializerCollector arrayInitializerCollector = new JavaDSLArrayInitializerCollector();
      if (node.getVariableDeclarators().isEmpty()) {
        return;
      }
      else
        for (ASTVariableDeclarator variableDeclarator : node.getVariableDeclarators()) {
          if (JavaDSLHelper.isByteType(localVarType) || JavaDSLHelper.isCharType(localVarType)
              || JavaDSLHelper.isShortType(localVarType)) {
            if (variableDeclarator.getVariableInitializer().isPresent()) {
              if (variableDeclarator.getVariableInitializer().get() instanceof ASTExpression) {
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
          }
          if (variableDeclarator.getVariableInitializer().isPresent()) {
            if(variableDeclarator.getVariableInitializer().get() instanceof  ASTArrayInitializer) {
              variableDeclarator.getVariableInitializer().get().accept(arrayInitializerCollector);
              List<ASTArrayInitializer> arrList = arrayInitializerCollector.getArrayInitializerList();
              if (localVarType.getDimension() == 0 && !arrList.isEmpty()) {
                Log.error("0xA0609 type mismatch, cannot convert from array to type '" + localVarType
                    .getName() + "'.", node.get_SourcePositionStart());
                return;
              }
              if (localVarType.getDimension() > 0 && arrList.size() == 0) {
                Log.error("0xA0609 type mismatch, array is expected.",
                    node.get_SourcePositionStart());
                return;
              }
              if (localVarType.getDimension() > 0 && localVarType.getDimension() == arrList.size()) {
                for (ASTArrayInitializer arrayInitializer : arrList) {
                  for(ASTVariableInitializer variableInitializer : arrayInitializer.getVariableInitializers()) {
                    typeResolver.handle(variableInitializer);
                    if (typeResolver.getResult().isPresent()) {
                      JavaTypeSymbolReference arrType = typeResolver.getResult().get();
                      if (!JavaDSLHelper.isReifiableType(arrType)) {
                        Log.error("Array component must be a reifiable type.",
                            node.get_SourcePositionStart());
                        return;
                      }
                      else {
                        JavaTypeSymbolReference componentType = JavaDSLHelper
                            .getComponentType(localVarType);
                        if (JavaDSLHelper.safeAssignmentConversionAvailable(arrType, componentType)) {
                          return;
                        }
                        else if (JavaDSLHelper
                            .unsafeAssignmentConversionAvailable(arrType, componentType)) {
                          Log.warn(
                              "0xA0610 Possible unchecked conversion from type '" + componentType
                                  .getName()
                                  + "' to '"
                                  + localVarType.getName() + "'.", node.get_SourcePositionStart());
                          break;
                        }
                        else {
                          Log.error(
                              "0xA0611 cannot assign a value of type '" + arrType.getName() + "' to '"
                                  + componentType
                                  .getName() + "'.", node.get_SourcePositionStart());
                          break;
                        }
                      }
                    }
                  }
                }
              }
            }
            else {
              variableDeclarator.accept(typeResolver);
              //  typeResolver.handle();
                if (typeResolver.getResult().isPresent()) {
                  JavaTypeSymbolReference expType = typeResolver.getResult()
                      .get();
                  //JLS3 5.2-1
                  if (JavaDSLHelper.safeAssignmentConversionAvailable(expType, localVarType)) {
                    return;
                  }
                  //JLS3 4.4-2
                  else if (JavaDSLHelper.unsafeAssignmentConversionAvailable(expType, localVarType)) {
                    Log.warn(
                        "0xA0610 Possible unchecked conversion from type '" + expType.getName()
                            + "' to '"
                            + localVarType.getName() + "'.", node.get_SourcePositionStart());
                  }
                  else {
                    Log.error(
                        "0xA0611 cannot assign a value of type '" + expType.getName() + "' to '"
                            + localVarType
                            .getName() + "'.", node.get_SourcePositionStart());
                  }
              }
            }
          }
        }
    }
  }
}
