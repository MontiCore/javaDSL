/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.enums;

import de.monticore.java.javadsl._ast.ASTEnumDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTEnumDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

public class EnumMayNotBeAbstract implements JavaDSLASTEnumDeclarationCoCo {

  @Override
  public void check(ASTEnumDeclaration node) {
    Symbol symbol = node.getSymbol().orElse(null);
    if (symbol == null) {
      Log.error("ASTEnumDeclaration must have a Symbol.");
    }
    else {
      JavaTypeSymbol javaEnumTypeSymbol = (JavaTypeSymbol) symbol;
      if (javaEnumTypeSymbol.isAbstract()) {
        Log.error("An enum may not be abstract.", node.get_SourcePositionStart());
      }
    }
  }

}
