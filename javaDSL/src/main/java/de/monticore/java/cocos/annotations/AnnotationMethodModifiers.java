/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.annotations;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTAnnotationMethod;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTAnnotationMethodCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class AnnotationMethodModifiers implements JavaDSLASTAnnotationMethodCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public AnnotationMethodModifiers(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTAnnotationMethod node) {
    JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol();
    List<String> modifiers = new ArrayList<>();
    for (ASTModifier modifier : node.getModifierList()) {
      modifier.accept(typeResolver);
      JavaTypeSymbolReference modifierType = typeResolver.getResult().get();
      modifiers.add((modifierType.getName()));
    }
    if (methodSymbol.isFinal()) {
      Log.error(
          "0xA0101 method in an annotation type declaration must not be declared final at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (methodSymbol.isNative()) {
      Log.error(
          "0xA0102 method in an annotation type declaration must not be declared native at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (methodSymbol.isPrivate()) {
      Log.error(
          "0xA0103 method in an annotation type declaration must not be declared private at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (methodSymbol.isProtected()) {
      Log.error(
          "0xA0104 method in an annotation type declaration must not be declared protected at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (methodSymbol.isStatic()) {
      Log.error(
          "0xA0105 method in an annotation type declaration must not be declared static at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (methodSymbol.isStrictfp()) {
      Log.error(
          "0xA0106 method in an annotation type declaration must not be declared strictFP at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (methodSymbol.isSynchronized()) {
      Log.error(
          "0xA0107 method in an annotation type declaration must not be declared synchronized at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (modifiers.contains("transient")) {
      Log.error(
          "0xA0108 method in an annotation type declaration must not be declared transient at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
    if (modifiers.contains("volatile")) {
      Log.error(
          "0xA0109 method in an annotation type declaration must not be declared volatile at method '"
              + node.getName() + "'.", node.get_SourcePositionStart());
    }
  }
}
