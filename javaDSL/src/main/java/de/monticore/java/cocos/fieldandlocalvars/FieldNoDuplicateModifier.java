/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.fieldandlocalvars;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class FieldNoDuplicateModifier implements JavaDSLASTFieldDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public FieldNoDuplicateModifier(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 8.3.1-1
  @Override public void check(ASTFieldDeclaration node) {
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      if(modifier instanceof ASTPrimitiveModifier) {
        modifier.accept(typeResolver);
        JavaTypeSymbolReference modType = typeResolver.getResult()
            .get();
        if (modifiers.contains(modType.getName())) {
          Log.error("0xA0608 The modifier '" + modType.getName()
                  + "' is mentioned more than once in the field declaration.",
              node.get_SourcePositionStart());
        }
        else {
          modifiers.add(modType.getName());
        }
      }
    }
  }
}
