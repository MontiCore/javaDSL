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
public class MethodAbstractAndOtherModifiers implements JavaDSLASTMethodDeclarationCoCo {

  //JLS3 8.4.3-3
  @Override public void check(ASTMethodDeclaration node) {
    if (node.getSymbol().isPresent()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol().get();
      if (methodSymbol.isAbstract()) {
        if (methodSymbol.isPrivate()) {
          Log.error("0xA0802 abstract method must not be declared 'private'.",
              node.get_SourcePositionStart());
        }
        if (methodSymbol.isStatic()) {
          Log.error("0xA0803 abstract method must not be declared 'static'.",
              node.get_SourcePositionStart());
        }
        if (methodSymbol.isFinal()) {
          Log.error("0xA0804 abstract method must not be declared 'final'.",
              node.get_SourcePositionStart());
        }
        if (methodSymbol.isNative()) {
          Log.error("0xA0805 abstract method must not be declared 'native'.",
              node.get_SourcePositionStart());
        }
        if (methodSymbol.isStrictfp()) {
          Log.error("0xA0806 abstract method must not be declared 'strictfp'.",
              node.get_SourcePositionStart());
        }
        if (methodSymbol.isSynchronized()) {
          Log.error("0xA0807 abstract method must not be declared 'synchronized'.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
