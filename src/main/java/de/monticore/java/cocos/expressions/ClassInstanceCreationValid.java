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

import de.monticore.java.javadsl._ast.ASTAnonymousClass;
import de.monticore.java.javadsl._ast.ASTExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTExpressionCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 26.07.2016.
 */
public class ClassInstanceCreationValid implements JavaDSLASTExpressionCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassInstanceCreationValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTExpression node) {
    if (node.getCreator().isPresent() && node.getCreator().get() instanceof ASTAnonymousClass) {
      node.getCreator().get().accept(typeResolver);
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference creatorType = typeResolver.getResult().get();
        for (ActualTypeArgument actualTypeArgument : creatorType.getActualTypeArguments()) {
          if ("ASTWildcardType".equals(actualTypeArgument.getType().getName())) {
            // JLS3 14.21-1
            Log.error("0xA0527 a class cannot be instantiated with a wildcard type argument.",
                node.get_SourcePositionStart());
          }
        }
        ASTAnonymousClass ast = (ASTAnonymousClass) node.getCreator().get();
        if (creatorType.getEnclosingScope().resolve(creatorType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) creatorType.getEnclosingScope()
              .resolve(creatorType.getName(), JavaTypeSymbol.KIND).get();
          // JLS3 15.9.1-1
          if (ast.getClassCreatorRest().getClassBody().isPresent()) {
            // Anonymous Class
            if (typeSymbol.isFinal()) {
              Log.error("0xA0528 cannot create an instance of final class.",
                  node.get_SourcePositionStart());
            }
            if (typeSymbol.isEnum()) {
              Log.error("0xA0529 cannot create an instance of enum class.",
                  node.get_SourcePositionStart());
            }
            
          }
          else {
            // JLS3 15.9.1-3
            if (typeSymbol.isEnum()) {
              Log.error("0xA0530 cannot create an instance of enum class.",
                  node.get_SourcePositionStart());
            }
            if (typeSymbol.isAbstract()) {
              Log.error("0xA0531 cannot create an instance of abstract class.",
                  node.get_SourcePositionStart());
            }
          }
        }
      }
    }
  }
}
