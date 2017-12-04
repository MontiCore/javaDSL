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

import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.shiftexpressions._ast.ASTQualifiedNameExpression;
import de.monticore.shiftexpressions._cocos.ShiftExpressionsASTQualifiedNameExpressionCoCo;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

public class QualifiedNameValid implements ShiftExpressionsASTQualifiedNameExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public QualifiedNameValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTQualifiedNameExpression node) {
    String name = node.getName();
    Scope scope = node.getEnclosingScope().get();
    typeResolver.handle(node.getExpression());
    if(!typeResolver.getResult().isPresent()) {
      // expression could be a package. try to resolve the name
      Collection<JavaTypeSymbol> resolvedTypes = scope.resolveMany(name, JavaTypeSymbol.KIND);
      if(resolvedTypes.size() != 1) {
        Log.error("symbol " + name + " not found.", node.get_SourcePositionStart());
      }
    }
    else {
      typeResolver.handle(node);
      if(!typeResolver.getResult().isPresent()) {
        Log.error("symbol " + name + " not found.", node.get_SourcePositionStart());
      }
    }
  }

}
