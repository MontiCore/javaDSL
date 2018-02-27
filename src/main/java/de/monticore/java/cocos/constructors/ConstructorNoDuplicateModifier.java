/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.constructors;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTConstructorDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ConstructorNoDuplicateModifier implements JavaDSLASTConstructorDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ConstructorNoDuplicateModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 8.8.3-1
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    
    for (ASTModifier modifier : node.getModifierList()) {
      if (modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference modType = typeResolver.getResult()
            .get();
        if (modifiers.contains(modType.getName())) {
          Log.error("0xA0311 modifier '" + modType.getName()
              + "' is mentioned more than once in the constructor declaration '" + node.getName()
              + "'.", node.get_SourcePositionStart());
        }
        else {
          modifiers.add(modType.getName());
        }
      }
    }
  }
}
