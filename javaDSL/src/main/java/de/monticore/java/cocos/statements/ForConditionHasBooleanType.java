/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTCommonForControl;
import de.monticore.java.javadsl._ast.ASTForStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTForStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class ForConditionHasBooleanType implements JavaDSLASTForStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ForConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.14.1-1
  @Override public void check(ASTForStatement node) {
    if (node.getForControl() instanceof ASTCommonForControl) {
      ASTCommonForControl forControl = (ASTCommonForControl) node.getForControl();
      if (forControl.isPresentCondition()) {
        typeResolver.handle(forControl.getCondition());
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference typeOfCondition = typeResolver
              .getResult().get();
          if (!JavaDSLHelper.isBooleanType(typeOfCondition)) {
            Log.error("0xA0906 condition of for-loop must be a boolean expression.",
                node.get_SourcePositionStart());
          }
        }
      }
    }

  }
}
