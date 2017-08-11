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
package de.monticore.java.cocos.annotations;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTAnnotationTypeDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTAnnotationTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class AnnotationTypeModifiers implements JavaDSLASTAnnotationTypeDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public AnnotationTypeModifiers(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTAnnotationTypeDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifiers()) {
      if (modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference type = typeResolver.getResult().get();
        modifiers.add(type.getName());
        if (modifiers.contains("final")) {
          Log.error(
              "0xA0112 annotation type must not be declared final at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("native")) {
          Log.error(
              "0xA0113 annotation type must not be declared native at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("private")) {
          Log.error(
              "0xA0114 annotation type must not be declared private at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("protected")) {
          Log.error(
              "0xA0115 annotation type must not be declared protected at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("static")) {
          Log.error(
              "0xA0116 annotation type must not be declared static at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("synchronized")) {
          Log.error(
              "0xA0117 annotation type must not be declared synchronized at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("transient")) {
          Log.error(
              "0xA0118 annotation type must not be declared transient at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }
        if (modifiers.contains("volatile")) {
          Log.error(
              "0xA0119 annotation type must not be declared volatile at declaration of annotation '"
                  + node.getName() + "'.",
              node.get_SourcePositionStart());
        }

      }

    }

  }
}
