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
package de.monticore.java.cocos.classes;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 25.09.2016.
 */
public class NonAbstractClassImplementsAllAbstractMethods
    implements JavaDSLASTClassDeclarationCoCo {
    
  HCJavaDSLTypeResolver typeResolver;
  
  public NonAbstractClassImplementsAllAbstractMethods(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol().get();
      if (node.getSuperClass().isPresent()) {
        node.getSuperClass().get().accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference superType = typeResolver.getResult().get();
          if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
                .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
            if (!classSymbol.isAbstract() && superSymbol.isAbstract()) {
              for (JavaMethodSymbol superMethod : superSymbol.getMethods()) {
                List<JavaTypeSymbolReference> list = JavaDSLHelper
                    .getSubstitutedFormalParameters(superType,
                        JavaDSLHelper.getParameterTypes(superMethod));
                if (superMethod.isAbstract()
                    && !JavaDSLHelper.overrides(classSymbol.getMethods(), superMethod, list)) {
                  Log.error(
                      "0xA0221 abstract method '" + superMethod.getName() + "' of super class '"
                          + superSymbol.getName() + "' is not overridden in class '" + classSymbol
                              .getName()
                          + "'.",
                      node.get_SourcePositionStart());
                }
              }
            }
          }
        }
      }
    }
  }
}
