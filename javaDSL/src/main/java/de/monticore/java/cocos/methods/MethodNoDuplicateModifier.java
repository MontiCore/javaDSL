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
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class MethodNoDuplicateModifier implements JavaDSLASTMethodDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public MethodNoDuplicateModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.4.3-1
  @Override public void check(ASTMethodDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getMethodSignature().getModifierList()) {
      if(modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference modType = typeResolver.getResult()
            .get();
        if (modifiers.contains(modType.getName())) {
          Log.error(
              "0xA0818 modifier '" + modType.getName() + "' is declared more than once in method '"
                  + node.getMethodSignature().getName() + "'.", node.get_SourcePositionStart());
        }
        else {
          modifiers.add(modType.getName());
        }
      }
    }
  }
}
