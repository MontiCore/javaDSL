/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class GenericClassNotSubClassOfThrowable implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public GenericClassNotSubClassOfThrowable(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    typeResolver.handle(node);
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference classType = typeResolver.getResult().get();
      if (!classType.getActualTypeArguments().isEmpty() && JavaDSLHelper.isThrowable(classType)) {
        Log.error("0xA0220 generic class '" + node.getName()
            + "' may not subclass 'java.lang.Throwable'.", node.get_SourcePositionStart());
      }
    }
  }
}
