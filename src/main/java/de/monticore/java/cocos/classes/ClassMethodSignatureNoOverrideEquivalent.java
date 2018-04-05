/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 18.08.2016.
 */
public class ClassMethodSignatureNoOverrideEquivalent implements JavaDSLASTClassDeclarationCoCo {

  @Override
  //JLS3 8.4.2-1
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol();
      List<JavaMethodSymbol> methods = typeSymbol.getMethods();
      for (int i = 1; i < methods.size(); i++) {
        for (int j = 0; j < i; j++) {
          JavaMethodSymbol m1 = methods.get(i);
          JavaMethodSymbol m2 = methods.get(j);
          if (JavaDSLHelper.isSubSignature(m1, m2, null) || JavaDSLHelper.isSubSignature(m2, m1, null)) {
            if (JavaDSLHelper.containsParametrizedType(m1) || JavaDSLHelper.containsParametrizedType(m2)) {
              Log.error("0xA0211 erasure of method '" + methods.get(i).getName()
                      + "' is same with another method in class '" + node.getName() + "'.",
                  node.get_SourcePositionStart());
            }
            else {
              Log.error(
                  "0xA0212 method '" + m1.getName() + "' is duplicated in class '" + node.getName()
                      + "'.", node.get_SourcePositionStart());
            }
            break;
          }
        }
      }
    }
  }
}
