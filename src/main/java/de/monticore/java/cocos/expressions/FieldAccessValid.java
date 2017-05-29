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

import java.util.Collection;

import de.monticore.java.mcexpressions._ast.ASTPrimaryExpression;
import de.monticore.java.mcexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.java.mcexpressions._cocos.MCExpressionsASTQualifiedNameExpressionCoCo;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class FieldAccessValid implements MCExpressionsASTQualifiedNameExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public FieldAccessValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 15.11.1-1, JLS3 15.11.1-2, JLS3 15.11.1-3
  @Override
  public void check(ASTQualifiedNameExpression node) {
    if (node.getExpression() instanceof ASTPrimaryExpression) {
      ASTPrimaryExpression primaryExpression = (ASTPrimaryExpression) node.getExpression();
      if (primaryExpression.isThis()) {
        typeResolver.handle(primaryExpression);
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
            if (type.getSpannedScope().resolveMany(node.getName(), JavaFieldSymbol.KIND)
                .size() > 1) {
              Log.error("0xA0537 field access to '" + node.getName() + "' is ambiguous.",
                  node.get_SourcePositionStart());
            }
            else {
              return;
            }
          }
          
        }
      }
      if (primaryExpression.isSuper()) {
        typeResolver.handle(primaryExpression);
        if (typeResolver.getResult().isPresent()) {
          if (JavaDSLHelper.isPrimitiveType(typeResolver.getResult().get())) {
            Log.error("0xA0539 super class does not exist.", node.get_SourcePositionStart());
          }
        }
      }
      if (primaryExpression.getName().isPresent()) {
        typeResolver.handle(primaryExpression);
        if (typeResolver.getResult().isPresent()) {
          String typeName = typeResolver.getResult().get().getName();
          if (typeResolver.getResult().get().getDimension() > 0 && !node.getName()
              .equals("length")) {
            Log.error("0xA0542 cannot find symbol '" + node.getName() + "'.",
                node.get_SourcePositionStart());
          }
          if (typeResolver.getResult().get().getDimension() == 0 && JavaDSLHelper
              .visibleTypeSymbolFound(node.getEnclosingScope().get(),
                  typeName)
              .isPresent()) {
            JavaTypeSymbolReference type = JavaDSLHelper
                .visibleTypeSymbolFound(node.getEnclosingScope().get(),
                    typeName)
                .get();
            JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                .resolve(type.getName(), JavaTypeSymbol.KIND).get();
            if (node.getName().equals("super")) {
              if (!typeSymbol.getSuperClass().isPresent()) {
                Log.error(
                    "0xA0543 keyword 'super' is used but the class does not have a super class.",
                    node.get_SourcePositionStart());
              }
            }
            else if (typeSymbol.isEnum()) {
              String name = node.getName();
              Collection<JavaMethodSymbol> methods = typeSymbol.getSpannedScope().resolveMany(name,
                  JavaMethodSymbol.KIND);
              Collection<JavaTypeSymbol> constant = typeSymbol.getSpannedScope().resolveMany(name,
                  JavaTypeSymbol.KIND);
              if (methods.size() == 0) {
                if (constant.size() == 0) {
                  Log.error(
                      "0xA0544 constant '" + name + "' is not member of enum '"
                          + typeSymbol.getName() + "'.",
                      node.get_SourcePositionStart());
                }
                else if (constant.size() > 1) {
                  Log.error("access to constant '" + name + "' is ambiguous.",
                      node.get_SourcePositionStart());
                }
              }
            }
          }
        }
      }
    }
  }
  
}
