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
package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class MethodFormalParametersDifferentName implements JavaDSLASTMethodDeclarationCoCo {

  //JLS3 8.4.1-1
  @Override public void check(ASTMethodDeclaration node) {
    List<String> names = new ArrayList<>();
    if (node.getMethodSignature().getFormalParameters().formalParameterListingIsPresent()) {
      for (ASTFormalParameter formalParameter : node.getMethodSignature().getFormalParameters()
          .getFormalParameterListing().get().getFormalParameters()) {
        if (names.contains(formalParameter.getDeclaratorId().getName())) {
          Log.error("0xA0812 formal parameter '" + formalParameter.getDeclaratorId().getName()
                  + "' is already declared in method '" + node.getMethodSignature().getName() + "'.",
              node.get_SourcePositionStart());
        }
        else {
          names.add(formalParameter.getDeclaratorId().getName());
        }
      }
    }
  }
}