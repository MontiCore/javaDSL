/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class MethodOverride implements JavaDSLASTClassDeclarationCoCo {

  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol();
      List<JavaTypeSymbolReference> superTypes = JavaDSLHelper.getReferencedSuperTypes(classSymbol);
      for (JavaMethodSymbol classMethod : classSymbol.getMethods()) {
        for (JavaTypeSymbolReference superType : superTypes) {
          List<Symbol> overridden = JavaDSLHelper.overriddenMethodFoundInSuperTypes(classMethod, superType);
          if (!overridden.isEmpty()) {
            JavaTypeSymbol superSymbol = (JavaTypeSymbol) overridden.get(0);
            JavaMethodSymbol superMethod = (JavaMethodSymbol) overridden.get(1);
            JavaTypeSymbolReference retType = classMethod.getReturnType();
            JavaTypeSymbolReference returnType = new JavaTypeSymbolReference(JavaDSLHelper.getCompleteName(retType), retType.getEnclosingScope(), retType.getDimension());
            returnType.setActualTypeArguments(retType.getActualTypeArguments());
            JavaTypeSymbolReference substitutedReturnType = JavaDSLHelper.getSubstitutedReturnType(superType, superMethod.getReturnType());
            if (!JavaDSLHelper.isReturnTypeSubstitutable(returnType, substitutedReturnType)) {
              Log.error(
                      "0xA0820 the return type is not compatible with '" + superSymbol.getName() + "."
                              + superMethod.getName() + "'.", node.get_SourcePositionStart());
            }

            else if (!JavaDSLHelper.isSubType(returnType, substitutedReturnType)) {
              Log.warn(
                      "0xA0821 the return type '" + returnType.getName() + "' for '"
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
                    .methodDifferentSignatureAndSameErasure(classMethod, superType);
            if (!sameErasure.isEmpty()) {
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
