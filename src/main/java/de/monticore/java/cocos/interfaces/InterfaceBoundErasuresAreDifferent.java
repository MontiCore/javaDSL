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
package de.monticore.java.cocos.interfaces;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class InterfaceBoundErasuresAreDifferent implements JavaDSLASTInterfaceDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public InterfaceBoundErasuresAreDifferent(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 5.1.9-1
  @Override
  public void check(ASTInterfaceDeclaration node) {
    typeResolver.handle(node);
    JavaTypeSymbolReference classType = typeResolver.getResult()
        .get();
    for (ActualTypeArgument ac : classType.getActualTypeArguments()) {
      List<ActualTypeArgument> upperBounds = ac.getType().getActualTypeArguments();
      for (int i = 0; i < upperBounds.size(); i++) {
        for (int j = i + 1; j < upperBounds.size(); j++) {
          JavaTypeSymbolReference upi = (JavaTypeSymbolReference) upperBounds.get(i).getType();
          JavaTypeSymbolReference upj = (JavaTypeSymbolReference) upperBounds.get(j).getType();
          if (JavaDSLHelper.areEqual(JavaDSLHelper.getRawType(upi), JavaDSLHelper.getRawType(upj))) {
            Log.error("0xA0701 bounds of type-variable '" + ac.getType().getName()
                + "' must not have the same erasure.", node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
