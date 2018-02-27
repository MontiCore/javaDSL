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
public class MethodBodyAbsenceAndPresence implements JavaDSLASTMethodDeclarationCoCo {
  
  @Override
  public void check(ASTMethodDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol().get();
      // JLS3 8.4.7-1
      if (methodSymbol.isAbstract() && node.isPresentMethodBody()) {
        Log.error(
            "0xA0808 abstract method '" + methodSymbol.getName() + "' must not specify a body.");
      }
      // JLS3 8.4.7-1
      if (methodSymbol.isNative() && node.isPresentMethodBody()) {
        Log.error(
            "0xA0809 native method '" + methodSymbol.getName() + "' must not specify a body.");
      }
      // JLS3 8.4.7-2
      if (!methodSymbol.isAbstract() && !methodSymbol.isNative()
          && !node.isPresentMethodBody()) {
        Log.error("0xA0810 method '" + methodSymbol.getName() + "' must specify a body.",
            node.get_SourcePositionStart());
      }
    }
  }
}
