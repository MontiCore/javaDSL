/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.cocos.annotations;

import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 18.08.2016.
 */
public class AnnotationNameNotAsEnclosingType implements JavaDSLASTTypeDeclarationCoCo {
  
  // JLS3 9.6-1
  @Override
  public void check(ASTTypeDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol typeSymbol = (JavaTypeSymbol) node.getSymbol();
      for (JavaTypeSymbol innerType : JavaDSLHelper.getAllInnerTypes(typeSymbol)) {
        if (innerType.isAnnotation() && typeSymbol.getName().equals(innerType.getName())) {
          Log.error("0xA0111 annotation type can not be named as the enclosing type.",
              node.get_SourcePositionStart());
        }
      }
    }
  }
}
