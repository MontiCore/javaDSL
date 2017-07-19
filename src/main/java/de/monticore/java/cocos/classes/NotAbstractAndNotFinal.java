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
package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantsJavaDSL;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author: breuer $
 * @since TODO: add version number
 */
public class NotAbstractAndNotFinal implements JavaDSLASTClassDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1206 Abstract and final modifier may not occur at the same time.";

  @Override
  public void check(ASTClassDeclaration node) {

    boolean isAbstract = false;
    boolean isFinal = false;
    for (ASTModifier modifier : node.getModifiers()) {
      if (modifier instanceof ASTPrimitiveModifier) {
        ASTPrimitiveModifier primitiveModifier = (ASTPrimitiveModifier) modifier;

        if (primitiveModifier.getModifier() == ASTConstantsJavaDSL.ABSTRACT) {
          isAbstract = true;
        }
        else if (primitiveModifier.getModifier() == ASTConstantsJavaDSL.FINAL) {
          isFinal = true;
        }
      }
    }
    if (isAbstract && isFinal) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }

}
