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
package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.types.types._ast.ASTVoidType;
import de.se_rwth.commons.logging.Log;

/**
 * Created by MB
 */
public class MethodReturnVoid implements JavaDSLASTMethodDeclarationCoCo {
      
  @Override
  public void check(ASTMethodDeclaration node) {
    if (node.getMethodSignature().getReturnType() instanceof ASTVoidType
        && node.getMethodSignature().getDim().size() > 0) {
      Log.error("0xA1208 Invalid return type for '" + node.getMethodSignature().getName()
          + "'. The void type must have dimension 0.",
          node.get_SourcePositionStart());
    }
  }
}
