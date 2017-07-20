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

import de.monticore.java.javadsl._ast.ASTCommonForControl;
import de.monticore.java.javadsl._ast.ASTForStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTForStatementCoCo;
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
public class ForConditionHasBooleanType implements JavaDSLASTForStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ForConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.14.1-1
  @Override public void check(ASTForStatement node) {
    if (node.getForControl() instanceof ASTCommonForControl) {
      ASTCommonForControl forControl = (ASTCommonForControl) node.getForControl();
      if (forControl.conditionIsPresent()) {
        typeResolver.handle(forControl.getCondition().get());
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference typeOfCondition = typeResolver
              .getResult().get();
          if (!JavaDSLHelper.isBooleanType(typeOfCondition)) {
            Log.error("0xA0906 condition of for-loop must be a boolean expression.",
                node.get_SourcePositionStart());
          }
        }
      }
    }

  }
}
