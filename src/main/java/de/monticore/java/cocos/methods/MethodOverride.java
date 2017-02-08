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

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @version $$Revision: 26242 $$, $$Date: 2017-01-23 13:05:13 +0100 (Mon, 23 Jan 2017) $$
 * @since TODO
 */
public class MethodOverride implements JavaDSLASTClassDeclarationCoCo {

  @Override
  public void check(ASTClassDeclaration node) {
    if (node.symbolIsPresent()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol().get();
      for (JavaMethodSymbol classMethod : classSymbol.getMethods()) {
        for (JavaTypeSymbolReference superType : JavaDSLHelper.getReferencedSuperTypes(classSymbol)) {
          List<Symbol> overridden = JavaDSLHelper.overriddenMethodFoundInSuperTypes(classMethod, superType)
              .orElse(null);
          if (overridden != null) {
            JavaTypeSymbol superSymbol = (JavaTypeSymbol) overridden.get(0);
            JavaMethodSymbol superMethod = (JavaMethodSymbol) overridden.get(1);
            if (!JavaDSLHelper.isReturnTypeSubstitutable(classMethod.getReturnType(),
                JavaDSLHelper.getSubstitutedReturnType(superType, superMethod.getReturnType()))) {
              Log.error(
                  "0xA0820 the return type is not compatible with '" + superSymbol.getName() + "."
                      + superMethod.getName() + "'.", node.get_SourcePositionStart());
            }

            else if (!JavaDSLHelper.isSubType(classMethod.getReturnType(), superMethod.getReturnType())) {
              Log.warn(
                  "0xA0821 the return type '" + classMethod.getReturnType().getName() + "' for '"
                      + classMethod.getName() + "' from the type '" + classSymbol.getName()
                      + "' needs unchecked conversion to conform to '" + superMethod.getName()
                      + "' in type '" + superSymbol.getName() + "'.",
                  node.get_SourcePositionStart());
            }
            else if (!classMethod.isPublic() && superMethod.isPublic()) {
              Log.error(
                  "0xA0822 the visibility (public) of inherited method '" + superMethod.getName()
                      + "' is reduced.",
                  node.get_SourcePositionStart());
            }
            else if (superMethod.isProtected() && !classMethod.isPublic() && !classMethod
                .isProtected()) {
              Log.error(
                  "0xA0823 the visibility (protected) of inherited method '" + superMethod.getName()
                      + "' is reduced.",
                  node.get_SourcePositionStart());
            }
            else if (JavaDSLHelper.isDefaultMethod(superMethod) && classMethod.isPrivate()) {
              Log.error("0xA0824 the visibility (default or package access) of inherited method '"
                      + superMethod.getName() + "' is reduced.",
                  node.get_SourcePositionStart());
            }
          }
          else {
            List<Symbol> sameErasure = JavaDSLHelper
                .methodDifferentSignatureAndSameErasure(classMethod, superType).orElse(null);
            if (sameErasure != null) {
              JavaTypeSymbol superSymbol = (JavaTypeSymbol) sameErasure.get(0);
              JavaMethodSymbol methodSymbol = (JavaMethodSymbol) sameErasure.get(1);
              Log.error("0xA0825 the method '" + classMethod.getName() + " of type '" + classSymbol
                  .getName() + "' has the same erasure as the method '" + methodSymbol.getName()
                  + "' of type '" + superSymbol.getName() + "'.");
            }
          }

        }
      }
    }

  }
}
