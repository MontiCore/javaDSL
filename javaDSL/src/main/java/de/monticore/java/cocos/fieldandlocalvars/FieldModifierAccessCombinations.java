/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.fieldandlocalvars;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class FieldModifierAccessCombinations implements JavaDSLASTFieldDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public FieldModifierAccessCombinations(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.3.1-2
  @Override
  public void check(ASTFieldDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modType = typeResolver.getResult()
          .get();
      modifiers.add(modType.getName());
    }
    if (modifiers.contains("public") && modifiers.contains("private") && modifiers
        .contains("protected")) {
      Log.error("0xA0604 The variable cannot be public, private and protected.",
          node.get_SourcePositionStart());
    }
    else if (modifiers.contains("public") && modifiers.contains("private")) {
      Log.error("0xA0605 The variable cannot be public and private.",
          node.get_SourcePositionStart());
    }
    else if (modifiers.contains("public") && modifiers.contains("protected")) {
      Log.error("0xA0606 The variable cannot be public and protected.",
          node.get_SourcePositionStart());
    }
    else if (modifiers.contains("private") && modifiers.contains("protected")) {
      Log.error("0xA0607 The variable cannot be private and protected.",
          node.get_SourcePositionStart());
    }
  }
}
