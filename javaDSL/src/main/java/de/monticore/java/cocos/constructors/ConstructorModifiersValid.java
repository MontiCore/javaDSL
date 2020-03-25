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
 */
public class ConstructorModifiersValid implements JavaDSLASTConstructorDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ConstructorModifiersValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.9.1-1
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modifierType = typeResolver.getResult()
          .get();
      modifiers.add(modifierType.getName());
    }
    if (modifiers.contains("abstract")) {
      Log.error("0xA0302 a constructor cannot be declared 'abstract'.",
          node.get_SourcePositionStart());
    }
    if (modifiers.contains("final")) {
      Log.error("0xA0303 a constructor cannot be declared 'final'.",
          node.get_SourcePositionStart());
    }
    if (modifiers.contains("static")) {
      Log.error("0xA0304 a constructor cannot be declared 'static'.",
          node.get_SourcePositionStart());
    }
    if (modifiers.contains("native")) {
      Log.error("0xA0305 a constructor cannot be declared 'native'.",
          node.get_SourcePositionStart());
    }
  }
}
