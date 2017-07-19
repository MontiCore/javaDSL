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
package de.monticore.java.cocos.classes;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
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
public class ClassNoCircularity implements JavaDSLASTClassDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;


  public ClassNoCircularity(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTClassDeclaration node) {
    typeResolver.handle(node);
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference classType = typeResolver.getResult().get();
      List<JavaTypeSymbolReference> list = new ArrayList<>();
      if (JavaDSLHelper.classCircularityExist(classType, list)) {
        Log.error("0xA0213 a circularity exists in the hierarchy of type '" + node.getName() + "'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
