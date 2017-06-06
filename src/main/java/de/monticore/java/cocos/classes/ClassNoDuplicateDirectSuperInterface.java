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
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class ClassNoDuplicateDirectSuperInterface implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassNoDuplicateDirectSuperInterface(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    typeResolver.handle(node);
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference classType = typeResolver.getResult().get();
      List<JavaTypeSymbolReference> superTypes = JavaDSLHelper
          .getAllParametrizedSuperInterfaces(classType);
      for (int i = 0; i < superTypes.size(); i++) {
        for (int j = i; j < superTypes.size(); j++) {
          if (superTypes.get(i).getName().equals(superTypes.get(j).getName())
              && !JavaDSLHelper.areEqual(superTypes.get(i), superTypes.get(j))) {
            Log.error(
                "0xA0214 super interface '" + superTypes.get(i).getName()
                    + "' cannot be implemented more than once with different arguments in class '"
                    + node.getName() + "'.",
                node.get_SourcePositionStart());
            return;
          }
        }
      }
    }
  }
}
