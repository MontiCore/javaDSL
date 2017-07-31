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

import de.monticore.java.javadsl._ast.ASTCatchClause;
import de.monticore.java.javadsl._ast.ASTCatchExceptionsHandler;
import de.monticore.java.javadsl._ast.ASTTryStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTTryStatementCoCo;
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
public class CatchIsValid implements JavaDSLASTTryStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public CatchIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3_14.20-1
  @Override
  public void check(ASTTryStatement node) {
    if (node.getExceptionHandler() instanceof ASTCatchExceptionsHandler) {
      ASTCatchExceptionsHandler handler = (ASTCatchExceptionsHandler) node.getExceptionHandler();
      for (ASTCatchClause clause : handler.getCatchClauses()) {
        if (clause.getCatchType().getQualifiedNames().size() == 1) {
          typeResolver.handle(clause.getCatchType());
          JavaTypeSymbolReference expType = typeResolver.getResult().get();
          if (!JavaDSLHelper.isThrowable(expType)) {
            Log.error(
                "0xA0903 the type of catch clause in try-statement must be either Throwable or subtype of it.",
                node.get_SourcePositionStart());
          }
        }
        else {
          Log.error("0xA0904 catch clause must have exactly one parameter.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
