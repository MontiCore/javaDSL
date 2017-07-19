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

import de.monticore.java.javadsl._ast.ASTSynchronizedStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTSynchronizedStatementCoCo;
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
public class SynchronizedArgIsReftype implements JavaDSLASTSynchronizedStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public SynchronizedArgIsReftype(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.19-1
  @Override public void check(ASTSynchronizedStatement node) {
    typeResolver.handle(node.getExpression());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typExp = typeResolver.getResult()
          .get();
      if (JavaDSLHelper.isPrimitiveType(typExp)) {
        Log.error("0xA0918 expression in synchronized-statement must have a reference type.",
            node.get_SourcePositionStart());
      }
    }
  }
}
