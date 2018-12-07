/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class InterfaceBoundErasuresAreDifferent implements JavaDSLASTInterfaceDeclarationCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public InterfaceBoundErasuresAreDifferent(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 5.1.9-1
  @Override
  public void check(ASTInterfaceDeclaration node) {
    typeResolver.handle(node);
    JavaTypeSymbolReference classType = typeResolver.getResult()
        .get();
    for (ActualTypeArgument ac : classType.getActualTypeArguments()) {
      List<ActualTypeArgument> upperBounds = ac.getType().getActualTypeArguments();
      for (int i = 0; i < upperBounds.size(); i++) {
        for (int j = i + 1; j < upperBounds.size(); j++) {
          JavaTypeSymbolReference upi = (JavaTypeSymbolReference) upperBounds.get(i).getType();
          JavaTypeSymbolReference upj = (JavaTypeSymbolReference) upperBounds.get(j).getType();
          if (JavaDSLHelper.areEqual(JavaDSLHelper.getRawType(upi), JavaDSLHelper.getRawType(upj))) {
            Log.error("0xA0701 bounds of type-variable '" + ac.getType().getName()
                + "' must not have the same erasure.", node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
