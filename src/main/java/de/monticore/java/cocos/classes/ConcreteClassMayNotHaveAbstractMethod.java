/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

public class ConcreteClassMayNotHaveAbstractMethod implements JavaDSLASTClassDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1204 A Concrete class may not have an abstract method.";

  @Override
  public void check(ASTClassDeclaration node) {
    Symbol symbol = node.getSymbol().orElse(null);
    if (symbol == null) {
      Log.error("0xA1203 ASTClassDeclaration must have a Symbol.");
    }
    else {
      JavaTypeSymbol javaClassTypeSymbol = (JavaTypeSymbol) symbol;
      if (!javaClassTypeSymbol.isAbstract()) {
        for (JavaMethodSymbol javaMethodSymbol : javaClassTypeSymbol.getMethods()) {
          if (javaMethodSymbol.isAbstract()) {
            Log.error(ERROR_MESSAGE, javaMethodSymbol.getSourcePosition());
          }
        }
      }
    }
  }
}
