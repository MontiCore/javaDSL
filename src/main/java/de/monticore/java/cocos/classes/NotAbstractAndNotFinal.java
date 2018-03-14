/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._ast.ASTConstantsJavaDSL;
import de.monticore.java.javadsl._ast.ASTModifier;
import de.monticore.java.javadsl._ast.ASTPrimitiveModifier;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 * @author (last commit) $Author: breuer $
 * @since TODO: add version number
 */
public class NotAbstractAndNotFinal implements JavaDSLASTClassDeclarationCoCo {

  public static final String ERROR_MESSAGE = "0xA1206 Abstract and final modifier may not occur at the same time.";

  @Override
  public void check(ASTClassDeclaration node) {

    boolean isAbstract = false;
    boolean isFinal = false;
    for (ASTModifier modifier : node.getModifierList()) {
      if (modifier instanceof ASTPrimitiveModifier) {
        ASTPrimitiveModifier primitiveModifier = (ASTPrimitiveModifier) modifier;

        if (primitiveModifier.getModifier() == ASTConstantsJavaDSL.ABSTRACT) {
          isAbstract = true;
        }
        else if (primitiveModifier.getModifier() == ASTConstantsJavaDSL.FINAL) {
          isFinal = true;
        }
      }
    }
    if (isAbstract && isFinal) {
      Log.error(ERROR_MESSAGE, node.get_SourcePositionStart());
    }
  }

}
