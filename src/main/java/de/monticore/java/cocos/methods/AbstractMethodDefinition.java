/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.methods;

import de.monticore.java.javadsl._ast.ASTMethodDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTMethodDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 23.09.2016.
 */
public class AbstractMethodDefinition implements JavaDSLASTMethodDeclarationCoCo {
  
  @Override
  public void check(ASTMethodDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaMethodSymbol methodSymbol = (JavaMethodSymbol) node.getSymbol();
      if (methodSymbol.isAbstract() && node.isPresentEnclosingScope()) {
        String name = JavaDSLHelper.getEnclosingTypeSymbolName(node.getEnclosingScope());
        if (name != null
            && node.getEnclosingScope().resolve(name, JavaTypeSymbol.KIND).isPresent()) {
          JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getEnclosingScope()
              .resolve(name, JavaTypeSymbol.KIND).get();
          if (typeSymbol.isInterface()) {
            return;
          }
          if (typeSymbol.isClass() && !typeSymbol.isAbstract()) {
            Log.error("0xA0801 abstract method '" + methodSymbol.getName()
                + "' must be defined by an abstract class.");
          }
        }
      }
    }
  }
}
