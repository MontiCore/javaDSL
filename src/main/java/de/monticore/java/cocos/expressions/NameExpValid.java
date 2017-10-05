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

import de.monticore.mcexpressions._ast.ASTNameExpression;
import de.monticore.mcexpressions._cocos.MCExpressionsASTNameExpressionCoCo;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

public class NameExpValid implements MCExpressionsASTNameExpressionCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public NameExpValid (HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTNameExpression node) {
    typeResolver.handle(node);
    if (!typeResolver.getResult().isPresent()) {
      Log.error(node.getName() + " could not be found.", node.get_SourcePositionStart());
    }
  }
}
