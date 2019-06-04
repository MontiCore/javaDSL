/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ClassNoDuplicateDirectSuperInterface implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassNoDuplicateDirectSuperInterface(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    typeResolver.handle(node);
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference classType = typeResolver.getResult().get();
      List<JavaTypeSymbolReference> superTypes = JavaDSLHelper
          .getAllParametrizedSuperInterfaces(classType);
      for (int i = 0; i < superTypes.size(); i++) {
        for (int j = i; j < superTypes.size(); j++) {
          if (superTypes.get(i).getName().equals(superTypes.get(j).getName())
              && !JavaDSLHelper.areEqual(superTypes.get(i), superTypes.get(j))) {
            Log.error(
                "0xA0214 super interface '" + superTypes.get(i).getName()
                    + "' cannot be implemented more than once with different arguments in class '"
                    + node.getName() + "'.",
                node.get_SourcePositionStart());
            return;
          }
        }
      }
    }
  }
}
