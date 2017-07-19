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
package de.monticore.java.cocos.annotations;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 18.08.2016.
 */
public class AnnotationNameNotAsEnclosingType implements JavaDSLASTTypeDeclarationCoCo {
  
  // JLS3 9.6-1
  @Override
  public void check(ASTTypeDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaTypeSymbol innerType : JavaDSLHelper.getAllInnerTypes(typeSymbol)) {
        if (innerType.isAnnotation() && typeSymbol.getName().equals(innerType.getName())) {
          Log.error("0xA0111 annotation type can not be named as the enclosing type.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
