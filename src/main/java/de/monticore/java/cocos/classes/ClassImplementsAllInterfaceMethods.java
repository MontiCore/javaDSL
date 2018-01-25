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

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.types.types._ast.ASTType;
import de.se_rwth.commons.logging.Log;

/**
 *
 * @author (last commit) $$Author: breuer $$
 */
public class ClassImplementsAllInterfaceMethods implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassImplementsAllInterfaceMethods(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol classTypeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (ASTType type : node.getImplementedInterfaces()) {
        type.accept(typeResolver);
        JavaTypeSymbolReference interfaceType = typeResolver
            .getResult().get();
        if (node.getEnclosingScope().isPresent()) {
          if (node.getEnclosingScope().get()
              .resolve(interfaceType.getName(), JavaTypeSymbol.KIND).isPresent()) {
            JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                .resolve(interfaceType.getName(), JavaTypeSymbol.KIND).get();
            for (JavaMethodSymbol interfaceMethod : interfaceSymbol.getMethods()) {
              List<JavaTypeSymbolReference> list = JavaDSLHelper
                  .getSubstitutedFormalParameters(interfaceType,
                      JavaDSLHelper.getParameterTypes(interfaceMethod));
              List<JavaMethodSymbol> classMethods = new ArrayList<>(classTypeSymbol.getMethods());
              if (classTypeSymbol.getSuperClass().isPresent()) {
                JavaTypeSymbolReference superClass = classTypeSymbol.getSuperClass().get();
                if (node.getEnclosingScope().get()
                    .resolve(superClass.getName(), JavaTypeSymbol.KIND).isPresent()) {
                  JavaTypeSymbol superSymbol = (JavaTypeSymbol) node.getEnclosingScope().get()
                      .resolve(superClass.getName(),
                          JavaTypeSymbol.KIND)
                      .get();
                  if (superSymbol.isClass()) {
                    classMethods.addAll(superSymbol.getMethods());
                  }
                  
                }
              }
              if (JavaDSLHelper.overrides(classMethods, interfaceMethod, list)) {
                if (interfaceMethod.isStatic()) {
                  Log.error(
                      "0xA0204 static method '" + interfaceMethod.getName()
                          + "' of the interface '"
                          + interfaceSymbol.getName() + "' is overridden in class '"
                          + classTypeSymbol.getName() + ".",
                      node.get_SourcePositionStart());
                }
                else if (interfaceMethod.isPrivate()) {
                  Log.error(
                      "0xA0205 private method '" + interfaceMethod.getName()
                          + "' of the interface '"
                          + interfaceSymbol.getName() + "' is overridden in class '"
                          + classTypeSymbol.getName() + ".",
                      node.get_SourcePositionStart());
                }
              }
              else if (interfaceSymbol.getPackageName().equals(classTypeSymbol.getPackageName())
                  && (interfaceMethod.isPublic() || interfaceMethod.isProtected() || JavaDSLHelper
                      .isDefaultMethod(interfaceMethod))) {
                Log.error(
                    "0xA0206 method '" + interfaceMethod.getName() + "' of the interface '"
                        + interfaceSymbol.getName() + "' must be implemented in class '"
                        + classTypeSymbol.getName() + ".",
                    node.get_SourcePositionStart());
              }
              else if (!interfaceSymbol.getPackageName().equals(classTypeSymbol.getPackageName())
                  && (interfaceMethod.isPublic() || interfaceMethod.isProtected())) {
                Log.error(
                    "0xA0207 method '" + interfaceMethod.getName() + "' of the interface '"
                        + interfaceSymbol.getName() + "' must be implemented in class '"
                        + classTypeSymbol.getName() + ".",
                    node.get_SourcePositionStart());
              }
            }
          }
          else {
            Log.error(
                "0xA0208 Super Interface in class declaration of '" + node.getName()
                    + "' is not found.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
