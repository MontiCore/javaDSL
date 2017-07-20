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
package de.monticore.java.cocos.methods;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
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
public class MethodNoAccessPairModifier implements JavaDSLASTMethodDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public MethodNoAccessPairModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;

  }

  //JLS3 8.4.3-2
  @Override public void check(ASTMethodDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getMethodSignature().getModifiers()) {
      if(modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference modifierType = typeResolver.getResult()
            .get();
        modifiers.add(modifierType.getName());
      }
    }
    if (modifiers.contains("public") && modifiers.contains("protected") && modifiers
        .contains("private")) {
      Log.error(
          "0xA0814 modifiers 'public', 'protected' and 'private' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (modifiers.contains("public") && modifiers.contains("protected")) {
      Log.error(
          "0xA0815 modifiers 'public' and 'protected' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (modifiers.contains("public") && modifiers.contains("private")) {
      Log.error(
          "0xA0816 modifiers 'public' and 'private' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (modifiers.contains("private") && modifiers.contains("protected")) {
      Log.error(
          "0xA0817 modifiers 'private' and 'protected' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }

  }
}
