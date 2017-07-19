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
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 *  on 04.08.2016.
 */
public class ClassInnerInstanceCreationValid implements JavaDSLASTExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ClassInnerInstanceCreationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTExpression node) {
    if (node.expressionIsPresent() && node.innerCreatorIsPresent()) {
      typeResolver.handle(node.getExpression().get());
      if (typeResolver.getResult().isPresent()) {
        if (JavaDSLHelper.isPrimitiveType(typeResolver.getResult().get())) {
          Log.error("0xA0519 the class must be a reference type");
        }
        if (node.getInnerCreator().get().getName().contains(".")) {
          Log.error("0xA0520 Inner class must have a simple name.");
        }
        JavaTypeSymbolReference primaryType = typeResolver.getResult().get();
        if (node.getEnclosingScope().get().resolve(primaryType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
              .resolve(primaryType.getName(), JavaTypeSymbol.KIND).get();
          if (typeSymbol.getSpannedScope()
              .resolveMany(node.getInnerCreator().get().getName(), JavaTypeSymbol.KIND).size()
              > 1) {
            Log.error("0xA0521 inner class '" + node.getInnerCreator().get().getName()
                + "' is ambiguous.", node.get_SourcePositionStart());
          }
          else if (typeSymbol.getSpannedScope()
              .resolveMany(node.getInnerCreator().get().getName(), JavaTypeSymbol.KIND).isEmpty()) {
            Log.error("0xA0522 inner class '" + node.getInnerCreator().get().getName()
                    + "' does not exist in class '" + typeSymbol.getName() + "'.",
                node.get_SourcePositionStart());
          }
          else if (typeSymbol.getSpannedScope()
              .resolve(node.getInnerCreator().get().getName(), JavaTypeSymbol.KIND).isPresent()) {
            JavaTypeSymbol innerType = (JavaTypeSymbol) typeSymbol.getSpannedScope()
                .resolve(node.getInnerCreator().get().getName(), JavaTypeSymbol.KIND).get();
            //JLS3 15.9.1-2
            if (node.getInnerCreator().get().getClassCreatorRest().classBodyIsPresent()) {
              if (innerType.isFinal()) {
                Log.error("0xA0523 cannot create an instance of final inner class.",
                    node.get_SourcePositionStart());
              }
              if (innerType.isEnum()) {
                Log.error("0xA0524 cannot create an instance of inner enum class.",
                    node.get_SourcePositionStart());
              }
            }
            else {
              //JLS3 15.9.1-4
              if (innerType.isAbstract()) {
                Log.error("0xA0525 cannot create an instance of inner abstract class.",
                    node.get_SourcePositionStart());
              }
              if (innerType.isEnum()) {
                Log.error("0xA0526 cannot create an instance of inner enum class.",
                    node.get_SourcePositionStart());
              }
            }
          }
        }
      }
    }
  }
}
