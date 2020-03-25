/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class InterfaceInnerTypeNotNamedAsEnclosing implements
    JavaDSLASTInterfaceDeclarationCoCo {

  @Override public void check(ASTInterfaceDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbol innerType : JavaDSLHelper.getAllInnerTypes(typeSymbol)) {
        if (typeSymbol.getName().equals(innerType.getName())) {
          if (innerType.isInterface()) {
            Log.error("0xA0704 inner interface '" + typeSymbol.getName()
                    + "' must not be named same as enclosing interface.",
                node.get_SourcePositionStart());
          }
          if (innerType.isClass()) {
            Log.error("0xA0705 inner class '" + typeSymbol.getName()
                    + "' must not be named same as enclosing interface.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
