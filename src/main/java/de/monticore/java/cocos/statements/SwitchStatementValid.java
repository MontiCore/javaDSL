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

import de.monticore.java.javadsl._ast.ASTConstantExpressionSwitchLabel;
import de.monticore.java.javadsl._ast.ASTDefaultSwitchLabel;
import de.monticore.java.javadsl._ast.ASTSwitchBlockStatementGroup;
import de.monticore.java.javadsl._ast.ASTSwitchLabel;
import de.monticore.java.javadsl._ast.ASTSwitchStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTSwitchStatementCoCo;
import de.monticore.expressions.mcexpressions._ast.ASTNameExpression;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class SwitchStatementValid implements JavaDSLASTSwitchStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public SwitchStatementValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTSwitchStatement node) {
      typeResolver.handle(node.getExpression());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference typeSwitch = typeResolver.getResult()
            .get();
        //JLS3 14.11-1
        if (!JavaDSLHelper.isIntType(typeSwitch) &&
            !JavaDSLHelper.isCharType(typeSwitch) &&
            !JavaDSLHelper.isShortType(typeSwitch) &&
            !JavaDSLHelper.isByteType(typeSwitch) &&
            !JavaDSLHelper.isEnum(typeSwitch)) {
          Log.error(
              "0xA0917 switch expression in the switch-statement must be char, byte, short, int, Character,\n"
                  + "Byte, Short, Integer, or an enum type.", node.get_SourcePositionStart());
        }
        int defaultNum = 0;
        for (ASTSwitchBlockStatementGroup switchBlockStatementGroup : node
            .getSwitchBlockStatementGroups()) {
          for (ASTSwitchLabel switchLabel : switchBlockStatementGroup.getSwitchLabels()) {
            //JLS3 14.11-2
            if (switchLabel == null) {
              Log.error("0xA0914 switch-label must be not null", node.get_SourcePositionStart());
            }
            if (switchLabel instanceof ASTDefaultSwitchLabel) {
              defaultNum = defaultNum + 1;
              //JLS3 14.11-2
              if (defaultNum > 1) {
                Log.error("0xA0915 switch-statement must have at most one default label.",
                    node.get_SourcePositionStart());
              }
            }
            else {
              ASTConstantExpressionSwitchLabel constant = (ASTConstantExpressionSwitchLabel) switchLabel;
              if (JavaDSLHelper.isEnum(typeSwitch) && constant.getConstantExpression() instanceof ASTNameExpression) {
                // TODO MB 
                String enumMember = ((ASTNameExpression)constant.getConstantExpression())
                    .getName();
                JavaTypeSymbolReference memberType = new JavaTypeSymbolReference(enumMember,
                    typeSwitch.getEnclosingScope(), 0);
                //JLS3 14.11-2
                if (!JavaDSLHelper.isEnumMember(typeSwitch, memberType)) {
                  Log.error(
                      "0xA0916 type of the case-label is incompatible with the type of the switch-statement.",
                      node.get_SourcePositionStart());
                }
              }
              else {
                typeResolver.handle(constant);
                if (typeResolver.getResult().isPresent()) {
                  JavaTypeSymbolReference typeLabel = typeResolver
                      .getResult().get();
                  //JLS3 14.11-2
                  if (!JavaDSLHelper.assignmentConversionAvailable(typeLabel, typeSwitch)) {
                    Log.error(
                        "0xA0916 type of the case-label is incompatible with the type of the switch-statement.",
                        node.get_SourcePositionStart());
                  }
                }
              }
            }
          }
        }
      }
    }
  
}
