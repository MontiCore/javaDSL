/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class ClassNoFinalSuperClass implements JavaDSLASTClassDeclarationCoCo {

  //JLS3 8.1.1-2
  @Override public void check(ASTClassDeclaration node) {
    JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol();
    if (classSymbol.getSuperClass().isPresent()) {
      JavaTypeSymbolReference superType = classSymbol.getSuperClass().get();
      if (node.isPresentEnclosingScope()) {
        if (node.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
            .isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
              .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
          if (typeSymbol.isFinal()) {
            Log.error("0xA0215 class is extending a final class '" + typeSymbol.getName() + "'.",
                node.get_SourcePositionStart());
          }
        }
        else {
          Log.error(
              "0xA0216 super class in class declaration of '" + node.getName() + "' is not found.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
