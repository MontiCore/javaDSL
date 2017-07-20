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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTExpressionCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class MethodInvocationValid implements JavaDSLASTExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public MethodInvocationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.12.1-1, JLS3 15.12.1-2, JLS3 15.12.1-3, JLS3 15.12.1-4, JLS3 15.12.1-5
  @Override public void check(ASTExpression node) {
    if (node.callExpressionIsPresent()) {
      List<JavaTypeSymbolReference> paramTypes = new ArrayList<>();
      for (ASTExpression astExpression : node.getParameterExpression()) {
        typeResolver.handle(astExpression);
        if (typeResolver.getResult().isPresent()) {
          paramTypes.add(typeResolver.getResult().get());
        }
        else {
          Log.error("0xA0560 Method parameter has an illegal type.",
              node.get_SourcePositionStart());
          return;
        }
      }
      ASTExpression callExpression = node.getCallExpression().get();
      if (callExpression.primaryExpressionIsPresent()) {
        //b(...);
        if (callExpression.getPrimaryExpression().get().nameIsPresent()) {
          String name = callExpression.getPrimaryExpression().get().getName().get();
          String enclosingSymbolName = JavaDSLHelper.getEnclosingTypeSymbolName(callExpression.getPrimaryExpression().get());
          JavaTypeSymbol enclosingSymbol = (JavaTypeSymbol) node.getEnclosingScope().get().resolve(enclosingSymbolName, JavaTypeSymbol.KIND).get();
          HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
              .resolveManyInSuperType(name, false, null, enclosingSymbol, null,
                  paramTypes);
          if (methodSymbols
              .isEmpty()) {
            Log.error("0xA0561 the method '" + name + "' is not found.",
                node.get_SourcePositionStart());
          }
          if (methodSymbols
              .size() > 1) {
            Log.error("0xA0562 the invocation of method '" + name + "' is ambiguous.",
                node.get_SourcePositionStart());
          }
        }
      }
      else if (callExpression.expressionIsPresent() && callExpression.nameIsPresent()) {
        String name = callExpression.getName().get();
        if (callExpression.getExpression().get().primaryExpressionIsPresent()) {
          if (callExpression.getExpression().get().getPrimaryExpression().get().nameIsPresent()) {
            String symbolName = callExpression.getExpression().get().getPrimaryExpression().get().getName().get();
            typeResolver
                .handle(callExpression.getExpression().get());
            JavaTypeSymbolReference type = typeResolver.getResult().get();
            if(node.getEnclosingScope().get().resolve(type.getName(), JavaTypeSymbol.KIND).isPresent()) {
              JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                  .resolve(type.getName(), JavaTypeSymbol.KIND).get();
              if(type.getName().equals(symbolName) || type.getName().endsWith(symbolName)) {
                HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                    .resolveManyInSuperType(name, true, null, typeSymbol, null,
                        paramTypes);
                if (methodSymbols
                    .isEmpty()) {
                  Log.error("0xA0563 method '" + name + "' is not found.",
                      node.get_SourcePositionStart());
                }
                if (methodSymbols
                    .size() > 1) {
                  Log.error("0xA0564 the invocation of method '" + typeSymbol.getName() + "." + name
                      + "' is ambiguous.", node.get_SourcePositionStart());
                }
              }
              else {
                HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                    .resolveManyInSuperType(name, false, type, typeSymbol, null,
                        paramTypes);
                if (methodSymbols.isEmpty()) {
                  Log.error("0xA0565 method '" + name + "' is not found.",
                      node.get_SourcePositionStart());
                }
                if (methodSymbols.size() > 1) {
                  Log.error("0xA0566 the invocation of method '" + name + "' is ambiguous.",
                      node.get_SourcePositionStart());
                }
              }
            }
            else {
              Log.error("symbol " + type.getName() + " is not found.", node.get_SourcePositionStart());
            }
          }
          else {
            typeResolver.handle(callExpression.getExpression().get());
            if (typeResolver.getResult().isPresent()) {
              JavaTypeSymbolReference type = typeResolver.getResult().get();
         //     String completeName = JavaDSLHelper.getCompleteName(type);
              if (node.getEnclosingScope().get().resolveMany(type.getName(), JavaTypeSymbol.KIND)
                  .size() == 1) {
                JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                    .resolve(type.getName(), JavaTypeSymbol.KIND).get();
                HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                    .resolveManyInSuperType(name, false, null, typeSymbol, null,
                        paramTypes);
                if (methodSymbols.isEmpty()) {
                  Log.error("0xA0567 method '" + name + "' is not found.",
                      node.get_SourcePositionStart());
                }
                if (methodSymbols.size() > 1) {
                  Log.error("0xA0568 the invocation of method '" + name + "' is ambiguous.",
                      node.get_SourcePositionStart());
                }
              }
            }
          }
        }
        else {
          typeResolver.handle(callExpression.getExpression().get());
          if (typeResolver.getResult().isPresent()) {
            JavaTypeSymbolReference expType = typeResolver.getResult().get();
      //      String completeName = JavaDSLHelper.getCompleteName(expType);
            if (node.getEnclosingScope().get().resolveMany(expType.getName(), JavaTypeSymbol.KIND)
                .size() == 1) {
              JavaTypeSymbol expSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                  .resolve(expType.getName(), JavaTypeSymbol.KIND).get();
              HashMap<JavaMethodSymbol, JavaTypeSymbolReference> methodSymbols = JavaDSLHelper
                  .resolveManyInSuperType(name, false, expType, expSymbol, null,
                      paramTypes);
              if (methodSymbols.isEmpty()) {
                Log.error("0xA0569 method '" + name + "' is not found.",
                    node.get_SourcePositionStart());
              }
              if (methodSymbols.size() > 1) {
                Log.error("0xA0570 the invocation of method '" + name + "' is ambiguous.",
                    node.get_SourcePositionStart());
              }
            } else {
              Log.error("0xA0581 method '" + name + "' is not found.",
                  node.get_SourcePositionStart());
            }
          } else {
            Log.error("0xA0574 method '" + name + "' is not found.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
