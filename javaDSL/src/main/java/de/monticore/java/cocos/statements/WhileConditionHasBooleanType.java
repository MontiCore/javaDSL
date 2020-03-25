/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTWhileStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTWhileStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class WhileConditionHasBooleanType implements JavaDSLASTWhileStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public WhileConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.12-1
  @Override public void check(ASTWhileStatement node) {
    typeResolver.handle(node.getCondition());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typeOfWhileStatement = typeResolver
          .getResult().get();
      if (!JavaDSLHelper.isBooleanType(typeOfWhileStatement)) {
        Log.error("0xA0919 condition in while-statement must be boolean expression.",
            node.get_SourcePositionStart());
      }
    }

  }
}
