/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.classes;

import java.util.List;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaMethodSymbol;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.se_rwth.commons.logging.Log;

/**
 *  on 25.09.2016.
 */
public class NonAbstractClassImplementsAllAbstractMethods
    implements JavaDSLASTClassDeclarationCoCo {
    
  HCJavaDSLTypeResolver typeResolver;
  
  public NonAbstractClassImplementsAllAbstractMethods(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSymbol()) {
      JavaTypeSymbol classSymbol = (JavaTypeSymbol) node.getSymbol().get();
      if (node.isPresentSuperClass()) {
        node.getSuperClass().accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference superType = typeResolver.getResult().get();
          if (superType.getEnclosingScope().resolve(superType.getName(), JavaTypeSymbol.KIND)
              .isPresent()) {
            JavaTypeSymbol superSymbol = (JavaTypeSymbol) superType.getEnclosingScope()
                .resolve(superType.getName(), JavaTypeSymbol.KIND).get();
            if (!classSymbol.isAbstract() && superSymbol.isAbstract()) {
              for (JavaMethodSymbol superMethod : superSymbol.getMethods()) {
                List<JavaTypeSymbolReference> list = JavaDSLHelper
                    .getSubstitutedFormalParameters(superType,
                        JavaDSLHelper.getParameterTypes(superMethod));
                if (superMethod.isAbstract()
                    && !JavaDSLHelper.overrides(classSymbol.getMethods(), superMethod, list)) {
                  Log.error(
                      "0xA0221 abstract method '" + superMethod.getName() + "' of super class '"
                          + superSymbol.getName() + "' is not overridden in class '" + classSymbol
                              .getName()
                          + "'.",
                      node.get_SourcePositionStart());
                }
              }
            }
          }
        }
      }
    }
  }
}
