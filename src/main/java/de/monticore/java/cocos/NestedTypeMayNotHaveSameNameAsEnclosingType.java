/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos;

import java.util.Collection;
import java.util.Map.Entry;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @since TODO: add version number
 */
public class NestedTypeMayNotHaveSameNameAsEnclosingType implements JavaDSLASTTypeDeclarationCoCo {

  public static final String ERROR_MESSAGE = "A nested type may not have the same name as its enclosing class";

  @Override
  public void check(ASTTypeDeclaration node) {
    JavaTypeSymbol javaTypeSymbol = (JavaTypeSymbol) node.getSymbol();
    for (Entry<String, Collection<Symbol>> entry : javaTypeSymbol.getSpannedScope()
        .getLocalSymbols().entrySet()) {
      for (Symbol nestedSymbol : entry.getValue()) {
        if (nestedSymbol instanceof JavaTypeSymbol) {
          JavaTypeSymbol nestedTypeSymbol = (JavaTypeSymbol) nestedSymbol;
          if (nestedTypeSymbol.getName().equals(javaTypeSymbol.getName())) {
            Log.error(ERROR_MESSAGE, nestedTypeSymbol.getAstNode().get().get_SourcePositionStart());
          }
        }
      }
    }
  }
}
