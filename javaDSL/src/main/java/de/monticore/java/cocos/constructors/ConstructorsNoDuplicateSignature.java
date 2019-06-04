/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.constructors;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 * TODO
 *
 * @author (last commit) $$Author: breuer $$
 * @since TODO
 */
public class ConstructorsNoDuplicateSignature implements
    JavaDSLASTClassDeclarationCoCo {


  //JLS3 8.8.2-2
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol();
      List<JavaMethodSymbol> constructors = typeSymbol.getConstructors();
      for (int i = 1; i < constructors.size(); i++) {
        for (int j = 0; j < i; j++) {
          JavaMethodSymbol c1 = constructors.get(i);
          JavaMethodSymbol c2 = constructors.get(j);
          if (JavaDSLHelper.isSubSignature(c1, c2, null) || JavaDSLHelper.isSubSignature(c2, c1, null)) {
            if (JavaDSLHelper.containsParametrizedType(c1) || JavaDSLHelper.containsParametrizedType(c2)) {
              Log.error("0xA0312 erasure of constructor '" + constructors.get(i).getName()
                      + "' is same with another constructor in class '" + node.getName() + "'.",
                  node.get_SourcePositionStart());
            }
            else {
              Log.error("0xA0313 constructor '" + c1.getName() + "' is duplicated in class '" + node
                  .getName() + "'.", node.get_SourcePositionStart());
            }
            break;
          }
        }
      }
    }
  }
}

