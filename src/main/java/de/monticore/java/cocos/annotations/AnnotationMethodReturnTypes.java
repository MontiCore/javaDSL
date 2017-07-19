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

import de.monticore.java.javadsl._ast.ASTAnnotationMethod;
import de.monticore.java.javadsl._cocos.JavaDSLASTAnnotationMethodCoCo;
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
public class AnnotationMethodReturnTypes implements JavaDSLASTAnnotationMethodCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  // JLS3_9.6-2
  public AnnotationMethodReturnTypes(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTAnnotationMethod node) {
    node.getType().accept(typeResolver);
    JavaTypeSymbolReference type = typeResolver.getResult().get();
    if ("String".equals(type.getName()) || "Class".equals(type.getName())
        || "java.lang.String".equals(type.getName())
        || "java.lang.Class".equals(type.getName())) {
      return;
    }
    if (JavaDSLHelper.isPrimitiveType(type)) {
      return;
    }
    if (JavaDSLHelper.isEnum(type)) {
      return;
    }
    if (JavaDSLHelper.isAnnotation(type)) {
      return;
    }
    else {
      Log.error("0xA0110 return type of a method in an annotation type declaration " +
          "may only be a primitive, String, Class, an Annotation or an " +
          "Enum at method '" + node.getName() + "'.",
          node.get_SourcePositionStart());
    }
    
  }
}
