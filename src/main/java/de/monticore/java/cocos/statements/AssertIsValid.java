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
package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTAssertStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTAssertStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class AssertIsValid implements JavaDSLASTAssertStatementCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public AssertIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTAssertStatement node) {
    typeResolver.handle(node.getAssertion());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference assertType = typeResolver.getResult().get();
      //JLS3_14.10-1
      if (!JavaDSLHelper.isBooleanType(assertType)) {
        Log.error("0xA0901 assert-statement expression must have a boolean type.",
            node.get_SourcePositionStart());
      }
    }
    if (node.getMessage().isPresent()) {
      typeResolver.handle(node.getMessage().get());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference messageType = typeResolver.getResult().get();
        //JLS3_14.10-2
        if (JavaDSLHelper.isVoidType(messageType)) {
          Log.error("0xA0902 assert-statement message cannot be of type void.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
