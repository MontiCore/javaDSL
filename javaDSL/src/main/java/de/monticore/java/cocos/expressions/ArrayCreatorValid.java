/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.expressions;

import de.monticore.java.javadsl._ast.ASTArrayCreator;
import de.monticore.java.javadsl._cocos.JavaDSLASTArrayCreatorCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 31.08.2016.
 */
public class ArrayCreatorValid implements JavaDSLASTArrayCreatorCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ArrayCreatorValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3_15.10-2
  @Override
  public void check(ASTArrayCreator node) {
    typeResolver.handle(node.getCreatedName());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (!JavaDSLHelper.isReifiableType(type)) {
        Log.error("0xA0504 a type of an array must be a reifiable type.",
            node.get_SourcePositionStart());
      }
    }

  }
}
