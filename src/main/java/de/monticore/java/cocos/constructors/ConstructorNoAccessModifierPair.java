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

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTConstructorDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class ConstructorNoAccessModifierPair implements JavaDSLASTConstructorDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ConstructorNoAccessModifierPair(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 8.8.3-2
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> listModifier = new ArrayList<>();
    for (ASTModifier modifier : node.getModifiers()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference mod = typeResolver.getResult().get();
      listModifier.add(mod.getName());
    }
    if (listModifier.contains("public") && listModifier.contains("protected") && listModifier
        .contains("private")) {
      Log.error(
          "0xA0307 modifiers 'public', 'protected' and private are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (listModifier.contains("public") && listModifier.contains("protected")) {
      Log.error(
          "0xA0308 modifiers 'public' and 'protected' are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (listModifier.contains("public") && listModifier.contains("private")) {
      Log.error(
          "0xA0309 modifiers 'public' and 'private' are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (listModifier.contains("protected") && listModifier.contains("private")) {
      Log.error(
          "0xA0310 modifiers 'protected' and 'private' are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
  }
}
