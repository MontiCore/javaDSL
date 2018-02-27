/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ClassNotStatic implements JavaDSLASTClassDeclarationCoCo {

  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol().get();
      if (!typeSymbol.getInnerTypes().isEmpty()) {
        for (JavaTypeSymbol innerType : typeSymbol.getInnerTypes()) {
          innerType.setInnerType(true);
        }
      }
      if (!typeSymbol.isInnerType() && typeSymbol.isStatic()) {
        Log.error(
            "0xA0218 a non-member class is not allowed to be static in the declaration of the class '"
                + node.getName() + "'.", node.get_SourcePositionStart());
      }
      //isInnerType is set always false!!
      /*
      if (!typeSymbol.isInnerType() && typeSymbol.isStatic()) {
        Log.error("0xA0210 A non-member Class is not allowed to be static in the declaration of the Class '"
            + node.getName() + "'.", node.get_SourcePositionStart());
      }
      */
    }
  }
}
