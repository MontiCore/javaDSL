/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.constructors;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
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
public class ConstructorNoAccessModifierPair implements JavaDSLASTConstructorDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ConstructorNoAccessModifierPair(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  // JLS3 8.8.3-2
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> listModifier = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference mod = typeResolver.getResult().get();
      listModifier.add(mod.getName());
    }
    if (listModifier.contains("public") && listModifier.contains("protected") && listModifier
        .contains("private")) {
      Log.error(
          "0xA0307 modifiers 'public', 'protected' and private are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (listModifier.contains("public") && listModifier.contains("protected")) {
      Log.error(
          "0xA0308 modifiers 'public' and 'protected' are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (listModifier.contains("public") && listModifier.contains("private")) {
      Log.error(
          "0xA0309 modifiers 'public' and 'private' are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (listModifier.contains("protected") && listModifier.contains("private")) {
      Log.error(
          "0xA0310 modifiers 'protected' and 'private' are mentioned in the same constructor declaration '"
              + node.getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
  }
}
