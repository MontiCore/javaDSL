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
package de.monticore.java.cocos.fieldandlocalvars;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class FieldModifierAccessCombinations implements JavaDSLASTFieldDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public FieldModifierAccessCombinations(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.3.1-2
  @Override
  public void check(ASTFieldDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifiers()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modType = typeResolver.getResult()
          .get();
      modifiers.add(modType.getName());
    }
    if (modifiers.contains("public") && modifiers.contains("private") && modifiers
        .contains("protected")) {
      Log.error("0xA0604 The variable cannot be public, private and protected.",
          node.get_SourcePositionStart());
    }
    else if (modifiers.contains("public") && modifiers.contains("private")) {
      Log.error("0xA0605 The variable cannot be public and private.",
          node.get_SourcePositionStart());
    }
    else if (modifiers.contains("public") && modifiers.contains("protected")) {
      Log.error("0xA0606 The variable cannot be public and protected.",
          node.get_SourcePositionStart());
    }
    else if (modifiers.contains("private") && modifiers.contains("protected")) {
      Log.error("0xA0607 The variable cannot be private and protected.",
          node.get_SourcePositionStart());
    }
  }
}
