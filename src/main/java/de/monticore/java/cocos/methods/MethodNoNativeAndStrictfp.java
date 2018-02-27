/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class MethodNoNativeAndStrictfp implements JavaDSLASTMethodDeclarationCoCo {

  //JLS3 8.4.3-4
  @Override public void check(ASTMethodDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol().get();
      if (methodSymbol.isNative() && methodSymbol.isStrictfp()) {
        Log.error("0xA0819 method '" + node.getMethodSignature().getName()
                + "' must not be both 'native' and 'strictfp'.",
            node.get_SourcePositionStart());
      }
    }

  }
}
