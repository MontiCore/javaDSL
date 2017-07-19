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
import de.monticore.java.javadsl._ast.ASTPrimaryExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTExpressionCoCo;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.JavaDSLPredicate;
import de.monticore.symboltable.Symbol;
import de.monticore.symboltable.SymbolKind;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.function.Predicate;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class FieldAccessValid implements JavaDSLASTExpressionCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public FieldAccessValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.11.1-1, JLS3 15.11.1-2, JLS3 15.11.1-3
  @Override
  public void check(ASTExpression node) {
    if (node.expressionIsPresent() && node.nameIsPresent()) {
      if (node.getExpression().get().getPrimaryExpression().isPresent()) {
        ASTPrimaryExpression primaryExpression = node.getExpression().get().getPrimaryExpression()
            .get();
        if (primaryExpression.isThis()) {
          typeResolver.handle(node.getExpression().get());
          if (typeResolver.getResult().isPresent()) {
            if (JavaDSLHelper.isPrimitiveType(typeResolver.getResult().get())) {
              Log.error("0xA0536 the type of a primary access must be a reference type.",
                  node.get_SourcePositionStart());
            }
            JavaTypeSymbolReference expType = typeResolver.getResult().get();
            if (node.getEnclosingScope().get().resolve(expType.getName(), JavaTypeSymbol.KIND)
                .isPresent()) {
              JavaTypeSymbol type = (JavaTypeSymbol) node.getEnclosingScope().get()
                  .resolve(expType.getName(), JavaTypeSymbol.KIND).get();
              if (type.getSpannedScope().resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
                  .size()>1) {
                Log.error("0xA0537 field access to '" + node.getName().get() + "' is ambiguous.",
                    node.get_SourcePositionStart());
              }
//              if (type.getSpannedScope().resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
//                  .isEmpty()) {
//                Log.error(
//                    "0xA0538 field '" + node.getName().get() + "' does not exist in the scope.",
//                    node.get_SourcePositionStart());
//              }
              else {
                return;
              }
            }

          }
        }
        if (primaryExpression.isSuper()) {
          typeResolver.handle(node.getExpression().get());
          if (typeResolver.getResult().isPresent()) {
            if (JavaDSLHelper.isPrimitiveType(typeResolver.getResult().get())) {
              Log.error("0xA0539 super class does not exist.", node.get_SourcePositionStart());
            }
//            JavaTypeSymbolReference expType = typeResolver.getResult().get();
//            if (node.getEnclosingScope().get().resolve(expType.getName(), JavaTypeSymbol.KIND)
//                .isPresent()) {
//              JavaTypeSymbol type = (JavaTypeSymbol) node.getEnclosingScope().get()
//                  .resolve(expType.getName(), JavaTypeSymbol.KIND).get();
//              if (type.getSpannedScope().resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
//                  .size() > 1) {
//                Log.error(
//                    "0xA0540 field access to '" + node.getName().get() + "' in super class + '"
//                        + expType
//                        .getName() + "' is ambiguous.", node.get_SourcePositionStart());
//              }
//              if (type.getSpannedScope().resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
//                  .isEmpty()) {
//                Log.error(
//                    "0xA0541 field '" + node.getName().get() + "' does not exist in super class '"
//                        + expType
//                        .getName() + "'.", node.get_SourcePositionStart());
//              }
//              else {
//                return;
//              }
//            }
          }
        }
        if (primaryExpression.getName().isPresent()) {
          typeResolver.handle(node.getExpression().get());
          if (typeResolver.getResult().isPresent()) {
            String typeName = typeResolver.getResult().get().getName();
            if (typeResolver.getResult().get().getDimension() > 0 && !node.getName().get()
                .equals("length")) {
              Log.error("0xA0542 cannot find symbol '" + node.getName().get() + "'.",
                  node.get_SourcePositionStart());
            }
            if (typeResolver.getResult().get().getDimension() == 0 && JavaDSLHelper
                .visibleTypeSymbolFound(node.getEnclosingScope().get(),
                    typeName)
                .isPresent()) {
              JavaTypeSymbolReference type =  JavaDSLHelper
                  .visibleTypeSymbolFound(node.getEnclosingScope().get(),
                      typeName).get();
              JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get().resolve(type.getName(), JavaTypeSymbol.KIND).get();
              if (node.getName().get().equals("super")) {
                if (!typeSymbol.getSuperClass().isPresent()) {
                  Log.error(
                      "0xA0543 keyword 'super' is used but the class does not have a super class.",
                      node.get_SourcePositionStart());
                }
              }
              else if (typeSymbol.isEnum()) {
                String name = node.getName().get();
                Collection<JavaMethodSymbol> methods = typeSymbol.getSpannedScope().resolveMany(name, JavaMethodSymbol.KIND);
                Collection<JavaTypeSymbol> constant = typeSymbol.getSpannedScope().resolveMany(name, JavaTypeSymbol.KIND);
                if(methods.size() == 0) {
                  if(constant.size() == 0) {
                    Log.error(
                            "0xA0544 constant '" + name + "' is not member of enum '"
                                    + typeSymbol.getName() + "'.", node.get_SourcePositionStart());
                  } else if(constant.size() > 1) {
                    Log.error("access to constant '" + name + "' is ambiguous.", node.get_SourcePositionStart());
                  }
                }
              }
//              else if (
//                  typeSymbol.getSpannedScope()
//                      .resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
//                      .size() == 0) {
//                Log.error(
//                    "0xA0545 field '" + node.getName().get() + "' does not exist in class '"
//                        + typeSymbol
//                        .getName() + "'.", node.get_SourcePositionStart());
//              }
//              else if (typeSymbol.getSpannedScope()
//                  .resolveMany(node.getName().get(), JavaFieldSymbol.KIND)
//                  .size() > 1) {
//                Log.error(
//                    "0xA0546 field access to '" + node.getName().get() + "' in super class + '"
//                        + typeSymbol
//                        .getName() + "' is ambiguous.", node.get_SourcePositionStart());
//              }
            }
          }
        }
      }
    }
    if (node.getPrimaryExpression().isPresent()) {
      if (node.getPrimaryExpression().get().getName().isPresent()) {
        String name = node.getPrimaryExpression().get().getName().get();
        JavaDSLPredicate predicate = new JavaDSLPredicate(node);
        Collection<JavaFieldSymbol> localSymbols = node.getEnclosingScope().get().
                resolveMany(name, JavaFieldSymbol.KIND, predicate);
        if (localSymbols.size() > 1) {
          Log.error("0xA0548 field access to '" + name + "' is ambiguous.",
              node.get_SourcePositionStart());
        }
        else {
          return;
        }
      }
    }
  }
}
