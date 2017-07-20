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
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTConstructorDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ConstructorNoDuplicateModifier implements JavaDSLASTConstructorDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ConstructorNoDuplicateModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 8.8.3-1
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    
    for (ASTModifier modifier : node.getModifiers()) {
      if (modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference modType = typeResolver.getResult()
            .get();
        if (modifiers.contains(modType.getName())) {
          Log.error("0xA0311 modifier '" + modType.getName()
              + "' is mentioned more than once in the constructor declaration '" + node.getName()
              + "'.", node.get_SourcePositionStart());
        }
        else {
          modifiers.add(modType.getName());
        }
      }
    }
  }
}
