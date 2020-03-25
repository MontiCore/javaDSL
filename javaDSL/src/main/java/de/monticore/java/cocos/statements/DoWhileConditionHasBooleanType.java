/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTDoWhileStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTDoWhileStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class DoWhileConditionHasBooleanType implements JavaDSLASTDoWhileStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public DoWhileConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.13-1
  @Override
  public void check(ASTDoWhileStatement node) {
    typeResolver.handle(node.getCondition());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference type = typeResolver.getResult().get();
      if (!JavaDSLHelper.isBooleanType(type)) {
        Log.error("0xA0905 Condition in do-statement must be a boolean expression.",
            node.get_SourcePositionStart());
      }
    }
  }
}
