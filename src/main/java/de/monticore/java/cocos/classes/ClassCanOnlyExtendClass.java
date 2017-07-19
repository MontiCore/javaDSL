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

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ClassCanOnlyExtendClass implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassCanOnlyExtendClass(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.superClassIsPresent()) {
      node.getSuperClass().get().accept(typeResolver);
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (node.getEnclosingScope().isPresent()) {
        if (node.getEnclosingScope().get().resolve(type.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
              .resolve(type.getName(), JavaTypeSymbol.KIND).get();
          if (!typeSymbol.isClass()) {
            Log.error(
                "0xA0202 class is not extending class in declaration of '" + node.getName() + "'.",
                node.get_SourcePositionStart());
          }
        }
      }
      else {
        Log.error(
            "0xA0203 super class in class declaration of '" + node.getName() + "' is not found.",
            node.get_SourcePositionStart());
      }
    }
  }
}
