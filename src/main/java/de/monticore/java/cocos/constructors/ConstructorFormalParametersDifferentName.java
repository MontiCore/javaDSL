/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.constructors;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTConstructorDeclaration;
import de.monticore.java.javadsl._ast.ASTFormalParameter;
import de.monticore.java.javadsl._cocos.JavaDSLASTConstructorDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

/**
 *  on 18.08.2016.
 */
public class ConstructorFormalParametersDifferentName implements
    JavaDSLASTConstructorDeclarationCoCo {
    
  // JLS3 8.8.2-1
  @Override
  public void check(ASTConstructorDeclaration node) {
    List<String> names = new ArrayList<>();
    if (node.getFormalParameters().isPresentFormalParameterListing()) {
      for (ASTFormalParameter formalParameter : node.getFormalParameters()
          .getFormalParameterListing().getFormalParameterList()) {
        if (names.contains(formalParameter.getDeclaratorId().getName())) {
          Log.error("0xA0301 constructor '" + node.getName()
              + "' contains multiple formal parameters with name '" + formalParameter
                  .getDeclaratorId().getName()
              + "'.", node.get_SourcePositionStart());
        }
        else {
          names.add(formalParameter.getDeclaratorId().getName());
        }
      }
    }
  }
}
