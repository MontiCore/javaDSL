/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._ast.ASTThrows;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.se_rwth.commons.logging.Log;

/**
 *  on 26.09.2016.
 */
public class MethodExceptionThrows implements JavaDSLASTMethodDeclarationCoCo {

  HCJavaDSLTypeResolver typeResolver;

  public MethodExceptionThrows(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  @Override public void check(ASTMethodDeclaration node) {
    if (node.getMethodSignature().isPresentThrows()) {
      ASTThrows astThrows = node.getMethodSignature().getThrows();
      for (ASTMCQualifiedName name : astThrows.getMCQualifiedNameList()) {
        typeResolver.handle(name);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference type = typeResolver.getResult().get();
          if (!JavaDSLHelper.isThrowable(type)) {
            Log.error("0xA0811 no exception of type '" + type.getName()
                    + "' can be thrown. An exception must be a subtype of Throwable.",
                node.get_SourcePositionStart());
          }
        }
      }
    }
  }
}
