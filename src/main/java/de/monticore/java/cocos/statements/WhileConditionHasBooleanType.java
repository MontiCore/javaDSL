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
package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTWhileStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTWhileStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class WhileConditionHasBooleanType implements JavaDSLASTWhileStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public WhileConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.12-1
  @Override public void check(ASTWhileStatement node) {
    typeResolver.handle(node.getCondition());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typeOfWhileStatement = typeResolver
          .getResult().get();
      if (!JavaDSLHelper.isBooleanType(typeOfWhileStatement)) {
        Log.error("0xA0919 condition in while-statement must be boolean expression.",
            node.get_SourcePositionStart());
      }
    }

  }
}
