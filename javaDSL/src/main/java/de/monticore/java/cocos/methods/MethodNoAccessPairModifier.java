/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class MethodNoAccessPairModifier implements JavaDSLASTMethodDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public MethodNoAccessPairModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;

  }

  //JLS3 8.4.3-2
  @Override public void check(ASTMethodDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getMethodSignature().getModifierList()) {
      if(modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference modifierType = typeResolver.getResult()
            .get();
        modifiers.add(modifierType.getName());
      }
    }
    if (modifiers.contains("public") && modifiers.contains("protected") && modifiers
        .contains("private")) {
      Log.error(
          "0xA0814 modifiers 'public', 'protected' and 'private' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (modifiers.contains("public") && modifiers.contains("protected")) {
      Log.error(
          "0xA0815 modifiers 'public' and 'protected' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (modifiers.contains("public") && modifiers.contains("private")) {
      Log.error(
          "0xA0816 modifiers 'public' and 'private' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }
    if (modifiers.contains("private") && modifiers.contains("protected")) {
      Log.error(
          "0xA0817 modifiers 'private' and 'protected' are mentioned in method '" + node
              .getMethodSignature().getName() + "'.",
          node.get_SourcePositionStart());
      return;
    }

  }
}
