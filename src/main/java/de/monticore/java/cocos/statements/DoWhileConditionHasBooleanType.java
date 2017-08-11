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
package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTDoWhileStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTDoWhileStatementCoCo;
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
public class DoWhileConditionHasBooleanType implements JavaDSLASTDoWhileStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public DoWhileConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.13-1
  @Override
  public void check(ASTDoWhileStatement node) {
    typeResolver.handle(node.getCondition());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (!JavaDSLHelper.isBooleanType(type)) {
        Log.error("0xA0905 Condition in do-statement must be a boolean expression.",
            node.get_SourcePositionStart());
      }
    }
  }
}
