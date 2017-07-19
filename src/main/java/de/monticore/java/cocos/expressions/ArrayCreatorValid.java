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
package de.monticore.java.cocos.expressions;

import de.monticore.java.javadsl._ast.ASTArrayCreator;
import de.monticore.java.javadsl._cocos.JavaDSLASTArrayCreatorCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 31.08.2016.
 */
public class ArrayCreatorValid implements JavaDSLASTArrayCreatorCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ArrayCreatorValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3_15.10-2
  @Override
  public void check(ASTArrayCreator node) {
    typeResolver.handle(node.getCreatedName());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (!JavaDSLHelper.isReifiableType(type)) {
        Log.error("0xA0504 a type of an array must be a reifiable type.",
            node.get_SourcePositionStart());
      }
    }

  }
}
