/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTSynchronizedStatement;
import de.monticore.java.javadsl._cocos.JavaDSLASTSynchronizedStatementCoCo;
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
public class SynchronizedArgIsReftype implements JavaDSLASTSynchronizedStatementCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public SynchronizedArgIsReftype(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  //JLS3 14.19-1
  @Override public void check(ASTSynchronizedStatement node) {
    typeResolver.handle(node.getExpression());
    if (typeResolver.getResult().isPresent()) {
      JavaTypeSymbolReference typExp = typeResolver.getResult()
          .get();
      if (JavaDSLHelper.isPrimitiveType(typExp)) {
        Log.error("0xA0918 expression in synchronized-statement must have a reference type.",
            node.get_SourcePositionStart());
      }
    }
  }
}
