/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.statements;

import de.monticore.java.javadsl._ast.ASTResource;
import de.monticore.java.javadsl._ast.ASTTryStatementWithResources;
import de.monticore.java.javadsl._cocos.JavaDSLASTTryStatementWithResourcesCoCo;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.se_rwth.commons.logging.Log;

/**
 * TODO: Write me!
 *
 */
public class ResourceInTryStatementCloseable implements JavaDSLASTTryStatementWithResourcesCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ResourceInTryStatementCloseable(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTTryStatementWithResources node) {
    if (!node.getResourceList().isEmpty())
      for (ASTResource astResource : node.getResourceList()) {
        astResource.getMCType().accept(typeResolver);
        if (typeResolver.getResult().isPresent()) {
          JavaTypeSymbolReference typeOfResource = typeResolver.getResult().get();
          boolean isCloseable = false;
          if (!typeOfResource.getReferencedSymbol().getInterfaces().isEmpty()) {
            for (JavaTypeSymbolReference Interface : typeOfResource.getReferencedSymbol()
                .getInterfaces()) {
              if ("Closeable".equals(Interface.getName())
                  || "java.io.Closeable".equals(Interface.getName())) {
                isCloseable = true;
              }
            }
          }
          else {
            if (typeOfResource.getReferencedSymbol().getSuperClass().isPresent()) {
              JavaTypeSymbolReference superClass = typeOfResource.getReferencedSymbol()
                  .getSuperClass().get();
              superClasses: while (!("Object".equals(superClass.getName()))) {
                if (!superClass.getReferencedSymbol().getInterfaces().isEmpty()) {
                  for (JavaTypeSymbolReference Interface : superClass.getReferencedSymbol()
                      .getInterfaces()) {
                    if ("Closeable".equals(Interface.getName())
                        || "java.io.Closeable".equals(Interface.getName())) {
                      isCloseable = true;
                      break superClasses;
                    }
                  }
                }
                if (superClass.getReferencedSymbol().getSuperClass().isPresent()) {
                  superClass = superClass.getReferencedSymbol().getSuperClass().get();
                }
              }
            }
          }
          if (!isCloseable) {
            Log.error("0xA0920 resource in try-statement must be closeable.",
                node.get_SourcePositionStart());
          }
        }
      }
    
  }
  
}
