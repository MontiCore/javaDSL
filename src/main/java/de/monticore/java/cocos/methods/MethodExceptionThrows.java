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

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTThrows;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.types.types._ast.ASTQualifiedName;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 26.09.2016.
 */
public class MethodExceptionThrows implements JavaDSLASTMethodDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public MethodExceptionThrows(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTMethodDeclaration node) {
    if (node.getMethodSignature().getThrows().isPresent()) {
      ASTThrows astThrows = node.getMethodSignature().getThrows().get();
      for (ASTQualifiedName name : astThrows.getQualifiedNames()) {
        typeResolver.handle(name);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference type = typeResolver.getResult().get();
          if (!JavaDSLHelper.isThrowable(type)) {
            Log.error("0xA0811 no exception of type '" + type.getName()
                    + "' can be thrown. An exception must be a subtype of Throwable.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
