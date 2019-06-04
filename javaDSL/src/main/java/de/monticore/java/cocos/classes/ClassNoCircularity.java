/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import java.util.ArrayList;
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
public class ClassNoCircularity implements JavaDSLASTClassDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;


  public ClassNoCircularity(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override
  public void check(ASTClassDeclaration node) {
    typeResolver.handle(node);
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference classType = typeResolver.getResult().get();
      List<JavaTypeSymbolReference> list = new ArrayList<>();
      if (JavaDSLHelper.classCircularityExist(classType, list)) {
        Log.error("0xA0213 a circularity exists in the hierarchy of type '" + node.getName() + "'.",
            node.get_SourcePositionStart());
      }
    }
  }
}
