/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.interfaces;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 25.09.2016.
 */
public class InterfaceMethodSignatureNoOverrideEquivalent implements
    JavaDSLASTInterfaceDeclarationCoCo {

  //JLS3 8.4.2-1
  @Override public void check(ASTInterfaceDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol interfaceSymbol = (JavaTypeSymbol) node.getSymbol();
      List<JavaMethodSymbol> methods = interfaceSymbol.getMethods();
      for (int i = 1; i < methods.size(); i++) {
        for (int j = 0; j < i; j++) {
          JavaMethodSymbol m1 = methods.get(i);
          JavaMethodSymbol m2 = methods.get(j);
          if (JavaDSLHelper.isSubSignature(m1, m2, null) || JavaDSLHelper.isSubSignature(m2, m1, null)) {
            if (JavaDSLHelper.containsParametrizedType(m1) || JavaDSLHelper.containsParametrizedType(m2)) {
              Log.error("0xA0706 erasure of method '" + methods.get(i).getName()
                      + "' is same with another method in interface '" + node.getName() + "'.",
                  node.get_SourcePositionStart());
            }
            else {
              Log.error("0xA0707 method '" + m1.getName() + "' is duplicated in interface '" + node
                  .getName() + "'.", node.get_SourcePositionStart());
            }
            break;
          }
        }
      }
    }
  }
}
