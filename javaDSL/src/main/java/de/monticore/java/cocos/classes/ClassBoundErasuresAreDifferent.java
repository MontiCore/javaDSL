/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import java.util.ArrayList;
import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.se_rwth.commons.logging.Log;

/**
 *
 */
public class ClassBoundErasuresAreDifferent implements JavaDSLASTClassDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ClassBoundErasuresAreDifferent(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 5.1.9-1
  @Override
  public void check(ASTClassDeclaration node) {
    typeResolver.handle(node);
    JavaTypeSymbolReference classType = typeResolver.getResult()
        .get();
    for (ActualTypeArgument ac : classType.getActualTypeArguments()) {
      List<JavaTypeSymbolReference> upperBounds = new ArrayList<>();
      for (ActualTypeArgument acc : ac.getType().getActualTypeArguments()) {
        if (acc.isUpperBound()) {
          upperBounds.add((JavaTypeSymbolReference) acc.getType());
        }
      }
      for (int i = 0; i < upperBounds.size(); i++) {
        for (int j = i + 1; j < upperBounds.size(); j++) {
          JavaTypeSymbolReference upi = JavaDSLHelper.applyTypeErasure(upperBounds.get(i));
          JavaTypeSymbolReference upj = JavaDSLHelper.applyTypeErasure(upperBounds.get(j));
          if (JavaDSLHelper.areEqual(upi, upj)) {
            Log.error("0xA0201 bounds of type-variable " + ac.getType().getName()
                + " must not have same erasures.", node.get_SourcePositionStart());
          }
        }
      }
    }

  }
}
