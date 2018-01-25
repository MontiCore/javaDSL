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

import java.util.List;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTReturnStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.java.types.JavaDSLReturnStatementCollector;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ReturnTypeAssignmentIsValid implements JavaDSLASTMethodDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ReturnTypeAssignmentIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTMethodDeclaration node) {
    typeResolver.handle(node);
    JavaTypeSymbolReference typeOfMethod = typeResolver.getResult()
        .get();
    if (node.isMethodBodyPresent()) {
      JavaDSLReturnStatementCollector JavaDSLReturnStatementCollector = new JavaDSLReturnStatementCollector();
      node.getMethodBody().accept(JavaDSLReturnStatementCollector);
      JavaDSLReturnStatementCollector.handle(node.getMethodBody());
      List<ASTReturnStatement> returnStatements = JavaDSLReturnStatementCollector
          .getReturnStatementList();
      //JLS3 14.17-2
      if (JavaDSLHelper.isVoidType(typeOfMethod)) {
        for(ASTReturnStatement statement : returnStatements) {
          if(statement.isExpressionPresent()) {
            Log.error("0xA0910 unexpected return-statement with expression for method with void type.",
                node.get_SourcePositionStart());
          }
        }
      }
      //JLS3 14.17-2
      if (!JavaDSLHelper.isVoidType(typeOfMethod) && returnStatements.isEmpty()) {
        Log.error(
            "0xA0911 return-statement expected for method '" + node.getMethodSignature().getName()
                + "'.",
            node.get_SourcePositionStart());
      }
      if (!JavaDSLHelper.isVoidType(typeOfMethod) && !returnStatements.isEmpty()) {
        for (ASTReturnStatement returnStatement : JavaDSLReturnStatementCollector
            .getReturnStatementList()) {
          //JLS3 14.17-3
          if (!returnStatement.isExpressionPresent()) {
            Log.error("0xA0912 expression is missing in return-statement.",
                node.get_SourcePositionStart());
          }
          else {
            typeResolver.handle(returnStatement);
            if (typeResolver.getResult().isPresent()) {
              JavaTypeSymbolReference returnType = typeResolver.getResult()
                  .get();
              //JLS3 14.17-4
              if (!JavaDSLHelper.assignmentConversionAvailable(returnType, typeOfMethod)) {
                Log.error(String.format("0xA0913 %s type of method cannot have return type %s.",
                    typeOfMethod.getName(), returnType.getName()), node.get_SourcePositionStart());
              }
            }
          }
        }
      }
    }
  }
}


