/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ClassInnerTypeNotNamedAsEnclosing implements JavaDSLASTClassDeclarationCoCo {

  @Override public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbol typeSymbol : JavaDSLHelper.getAllInnerTypes(classSymbol)) {
        if (classSymbol.getName().equals(typeSymbol.getName())) {
          if (typeSymbol.isClass()) {
            Log.error("0xA0209 inner class must not be named same as enclosing class.",
                node.get_SourcePositionStart());
          }
          if (typeSymbol.isInterface()) {
            Log.error("0xA0210 inner interface must not be named same as enclosing class.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
