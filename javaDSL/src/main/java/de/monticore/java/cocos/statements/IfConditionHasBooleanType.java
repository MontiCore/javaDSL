/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTIfStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTIfStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class IfConditionHasBooleanType implements JavaDSLASTIfStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public IfConditionHasBooleanType(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.9-1
  @Override public void check(ASTIfStatement node) {
    typeResolver.handle(node.getCondition());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typeOfIfStatement = typeResolver.getResult()
          .get();
      if (!JavaDSLHelper.isBooleanType(typeOfIfStatement)) {
        Log.error("0xA0909 condition in if-statement must be a boolean expression.",
            node.get_SourcePositionStart());
      }
    }

  }
}
