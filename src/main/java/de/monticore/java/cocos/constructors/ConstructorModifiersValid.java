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
package de.monticore.java.cocos.constructors;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
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
public class ConstructorModifiersValid implements JavaDSLASTConstructorDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ConstructorModifiersValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.9.1-1
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifiers()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modifierType = typeResolver.getResult()
          .get();
      modifiers.add(modifierType.getName());
    }
    if (modifiers.contains("abstract")) {
      Log.error("0xA0302 a constructor cannot be declared 'abstract'.",
          node.get_SourcePositionStart());
    }
    if (modifiers.contains("final")) {
      Log.error("0xA0303 a constructor cannot be declared 'final'.",
          node.get_SourcePositionStart());
    }
    if (modifiers.contains("static")) {
      Log.error("0xA0304 a constructor cannot be declared 'static'.",
          node.get_SourcePositionStart());
    }
    if (modifiers.contains("native")) {
      Log.error("0xA0305 a constructor cannot be declared 'native'.",
          node.get_SourcePositionStart());
    }
  }
}
