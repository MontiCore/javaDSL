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

import de.monticore.java.javadsl._ast.ASTArrayInitializer;
import de.monticore.java.javadsl._ast.ASTVariableDeclarator;
import de.monticore.java.javadsl._ast.ASTVariableInitializer;
import de.monticore.java.javadsl._ast.ASTVariableInititializerOrExpression;
import de.monticore.java.javadsl._cocos.JavaDSLASTVariableDeclaratorCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

public class ArrayInitializerValid implements JavaDSLASTVariableDeclaratorCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ArrayInitializerValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 15.10-2
  @Override public void check(ASTVariableDeclarator node) {
    if (node.isVariableInititializerOrExpressionPresent() && node.getVariableInititializerOrExpression().isVariableInitializerPresent()) {
      ASTVariableInitializer varNode = node.getVariableInititializerOrExpression().getVariableInitializer();
      if (varNode instanceof ASTArrayInitializer) {
        ASTArrayInitializer arrInitializer = (ASTArrayInitializer) varNode;
        for (ASTVariableInititializerOrExpression var : arrInitializer.getVariableInititializerOrExpressionList()) {
          var.accept(typeResolver);
          JavaTypeSymbolReference extType = typeResolver.getResult()
              .get();
          if (!JavaDSLHelper.isReifiableType(extType)) {
            //10.6.1
            Log.error("0xA0506 an array component type must be a reifiable type.",
                node.get_SourcePositionStart());
            return;
          }
        }
      }
    }
  }
}
