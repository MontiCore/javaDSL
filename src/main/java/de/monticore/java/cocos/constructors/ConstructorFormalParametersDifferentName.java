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
package de.monticore.java.cocos.constructors;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._cocos.JavaDSLASTConstructorDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *  on 18.08.2016.
 */
public class ConstructorFormalParametersDifferentName implements
    JavaDSLASTConstructorDeclarationCoCo {
    
  // JLS3 8.8.2-1
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> names = new ArrayList<>();
    if (node.getFormalParameters().formalParameterListingIsPresent()) {
      for (ASTFormalParameter formalParameter : node.getFormalParameters()
          .getFormalParameterListing().get().getFormalParameters()) {
        if (names.contains(formalParameter.getDeclaratorId().getName())) {
          Log.error("0xA0301 constructor '" + node.getName()
              + "' contains multiple formal parameters with name '" + formalParameter
                  .getDeclaratorId().getName()
              + "'.", node.get_SourcePositionStart());
        }
        else {
          names.add(formalParameter.getDeclaratorId().getName());
        }
      }
    }
  }
}
