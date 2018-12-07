/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ClassNoModifierDuplicate implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassNoModifierDuplicate(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 8.1.1-1
  @Override
  public void check(ASTClassDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modType = typeResolver.getResult()
          .get();
      if (modifiers.contains(modType.getName())) {
        Log.error("0xA0217 modifier '" + modType.getName()
            + "' is mentioned more than once in the class declaration '" + node.getName() + "'.",
            node.get_SourcePositionStart());
      }
      else {
        modifiers.add(modType.getName());
      }
    }
    
  }
}
