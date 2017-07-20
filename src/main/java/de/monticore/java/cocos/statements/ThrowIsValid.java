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

import de.monticore.java.javadsl._ast.ASTThrowStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTThrowStatementCoCo;
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
public class ThrowIsValid implements JavaDSLASTThrowStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ThrowIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.18-1
  @Override
  public void check(ASTThrowStatement node) {
    typeResolver.handle(node.getExpression());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typeThrow = typeResolver.getResult()
          .get();
      if (!JavaDSLHelper.isThrowable(typeThrow)) {
        Log.error("0xA0918 exception in throw-statement must be Throwable or subtype of it.",
            node.get_SourcePositionStart());
      }
    }
  }
}
