/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class InterfaceNoDuplicateModifier implements JavaDSLASTInterfaceDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public InterfaceNoDuplicateModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.8.3-1
  @Override public void check(ASTInterfaceDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modType = typeResolver.getResult()
          .get();
      if (modifiers.contains(modType.getName())) {
        Log.error(
            "0xA0711 modifier '" + modType.getName() + "' is declared more than once in interface '"
                + node
                .getName() + "'.", node.get_SourcePositionStart());
      }
      else {
        modifiers.add(modType.getName());
      }
    }
  }
}
