/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTThrowStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTThrowStatementCoCo;
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
public class ThrowIsValid implements JavaDSLASTThrowStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public ThrowIsValid(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.18-1
  @Override
  public void check(ASTThrowStatement node) {
    typeResolver.handle(node.getExpression());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typeThrow = typeResolver.getResult()
          .get();
      if (!JavaDSLHelper.isThrowable(typeThrow)) {
        Log.error("0xA0918 exception in throw-statement must be Throwable or subtype of it.",
            node.get_SourcePositionStart());
      }
    }
  }
}
