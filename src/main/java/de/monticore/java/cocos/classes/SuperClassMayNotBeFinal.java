/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

/**
 *
 * @author (last commit) $Author: breuer $
 */
public class SuperClassMayNotBeFinal implements JavaDSLASTClassDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1207 A super class may not be final.";

  @Override
  public void check(ASTClassDeclaration node) {
    Symbol symbol = node.getSymbol().orElse(null);
    if (symbol == null) {
      Log.error("ASTClassDeclaration must have a Symbol.");
    }
    else {
      JavaTypeSymbol javaClassTypeSymbol = (JavaTypeSymbol) symbol;
      if (javaClassTypeSymbol.getSuperClass().isPresent()) {
        final JavaTypeSymbolReference superClass = javaClassTypeSymbol.getSuperClass().get();
        if (superClass.getReferencedSymbol().isFinal()) {
          Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
        }
      }
    }
  }
}
