/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos;

import java.util.HashSet;
import java.util.Set;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaFieldSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author$
 * @since TODO: add version number
 */
public class FieldNamesMustBePairWiseDifferent implements JavaDSLASTTypeDeclarationCoCo {
  public static final String ERROR_MESSAGE = "Field names must be pairwise different.";

  @Override
  public void check(ASTTypeDeclaration node) {
    Set<String> names = new HashSet<String>();
    JavaTypeSymbol javaTypeSymbol = (JavaTypeSymbol) node.getSymbol().get();
    for (JavaFieldSymbol javaFieldSymbol : javaTypeSymbol.getFields()) {
      String name = javaFieldSymbol.getName();
      if (names.contains(name)) {
        Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
      }
      else {
        names.add(name);
      }
    }
  }
}
