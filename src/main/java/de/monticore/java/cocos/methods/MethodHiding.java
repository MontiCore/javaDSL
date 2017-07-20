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
package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 *  on 24.09.2016.
 */
public class MethodHiding implements JavaDSLASTClassDeclarationCoCo {
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol classTypeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaTypeSymbolReference superType : classTypeSymbol.getSuperTypes()) {
        if (node.getEnclosingScope().get().resolve(superType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol superSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
              .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
          for (JavaMethodSymbol classMethod : classTypeSymbol.getMethods()) {
            for (JavaMethodSymbol superMethod : superSymbol.getMethods()) {
              List<JavaTypeSymbolReference> parameters = JavaDSLHelper
                  .getSubstitutedFormalParameters(superType,
                      JavaDSLHelper.getParameterTypes(superMethod));
              if (JavaDSLHelper.isSubSignature(classMethod, superMethod, parameters)
                  && classMethod.isStatic() && !superMethod.isStatic()
                  && JavaDSLHelper.overrides(classMethod, superMethod, parameters)) {
                Log.error("0xA0813 class method '" + classMethod.getName()
                    + "' is hiding an instance method.",
                    node.get_SourcePositionStart());
              }
            }
          }
        }
      }
    }
  }
}
