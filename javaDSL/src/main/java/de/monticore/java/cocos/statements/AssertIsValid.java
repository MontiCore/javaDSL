/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTAssertStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTAssertStatementCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 */
public class AssertIsValid implements JavaDSLASTAssertStatementCoCo {
  HCJavaDSLTypeResolver typeResolver;

  public AssertIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTAssertStatement node) {
    typeResolver.handle(node.getAssertion());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference assertType = typeResolver.getResult().get();
      //JLS3_14.10-1
      if (!JavaDSLHelper.isBooleanType(assertType)) {
        Log.error("0xA0901 assert-statement expression must have a boolean type.",
            node.get_SourcePositionStart());
      }
    }
    if (node.isPresentMessage()) {
      typeResolver.handle(node.getMessage());
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference messageType = typeResolver.getResult().get();
        //JLS3_14.10-2
        if (JavaDSLHelper.isVoidType(messageType)) {
          Log.error("0xA0902 assert-statement message cannot be of type void.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
