/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 24.09.2016.
 */
public class MethodHiding implements JavaDSLASTClassDeclarationCoCo {
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol classTypeSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbolReference superType : classTypeSymbol.getSuperTypes()) {
        if (node.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol superSymbol = (JavaTypeSymbol) node.getEnclosingScope()
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
