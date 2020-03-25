/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class MethodFormalParametersDifferentName implements JavaDSLASTMethodDeclarationCoCo {

  //JLS3 8.4.1-1
  @Override public void check(ASTMethodDeclaration node) {
    List<String> names = new ArrayList<>();
    if (node.getMethodSignature().getFormalParameters().isPresentFormalParameterListing()) {
      for (ASTFormalParameter formalParameter : node.getMethodSignature().getFormalParameters()
          .getFormalParameterListing().getFormalParameterList()) {
        if (names.contains(formalParameter.getDeclaratorId().getName())) {
          Log.error("0xA0812 formal parameter '" + formalParameter.getDeclaratorId().getName()
                  + "' is already declared in method '" + node.getMethodSignature().getName() + "'.",
              node.get_SourcePositionStart());
        }
        else {
          names.add(formalParameter.getDeclaratorId().getName());
        }
      }
    }
  }
}
