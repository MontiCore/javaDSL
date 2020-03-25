/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTLastFormalParameter;
import de.monticore.java.javadsl._cocos.JavaDSLASTLastFormalParameterCoCo;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 */
public class MethodFormalParametersVarargsNoArray implements JavaDSLASTLastFormalParameterCoCo {
  
  public static final String ERROR_MESSAGE = "Variable argument %s must not be an array";
  
  @Override
  public void check(ASTLastFormalParameter node) {
    if(!node.getDeclaratorId().getDimList().isEmpty()) {
      Log.error(String.format(ERROR_MESSAGE, node.getDeclaratorId().getName()), 
          node.get_SourcePositionStart());
    }
  }
}
