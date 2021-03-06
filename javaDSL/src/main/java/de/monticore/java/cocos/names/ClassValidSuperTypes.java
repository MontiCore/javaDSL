/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.names;

import java.util.Collection;

import de.monticore.java.javadsl._ast.ASTClassDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTClassDeclarationCoCo;
import de.monticore.java.symboltable.JavaTypeSymbol;
import de.monticore.java.symboltable.JavaTypeSymbolReference;
import de.monticore.java.types.HCJavaDSLTypeResolver;
import de.monticore.java.types.JavaDSLHelper;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.types._ast.ASTType;
import de.se_rwth.commons.logging.Log;

/**
 *  on 29.10.2016.
 */
public class ClassValidSuperTypes implements JavaDSLASTClassDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public ClassValidSuperTypes(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTClassDeclaration node) {
    if (node.isPresentSuperClass()) {
      node.getSuperClass().accept(typeResolver);
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference classType = typeResolver.getResult().get();
        
        if (!JavaDSLHelper.isPrimitiveType(classType)) {
          Collection<JavaTypeSymbol> typeSymbols = classType.getEnclosingScope()
              .resolveMany(classType.getName(), JavaTypeSymbol.KIND);
          if (typeSymbols.isEmpty()) {
            Log.error(
                "0xA0702 the symbol of super class '" + classType.getName() + "' is not found.",
                node.get_SourcePositionStart());
          }
          if (typeSymbols.size() > 1) {
            Log.error(
                "0xA0703 the symbol of super class '" + classType.getName() + "' is ambiguous.",
                node.get_SourcePositionStart());
          }
          if (typeSymbols.size() == 1) {
            JavaTypeSymbol classSymbol = typeSymbols.iterator().next();
            if (!classType.getActualTypeArguments().isEmpty()) {
              if (classSymbol.getFormalTypeParameters().isEmpty()) {
                Log.error(
                    "0xA0704 the class '" + classType.getName()
                        + "' is not generic. Cannot be parametrized.",
                    node.get_SourcePositionStart());
              }
              else if (classSymbol.getFormalTypeParameters().size() != classType
                  .getActualTypeArguments()
                  .size()) {
                Log.error("0xA0705 incorrect number of type arguments for the type '"
                    + classType.getName() + "'.",
                    node.get_SourcePositionStart());
              }
            }
          }
        }
      }
      else {
        Log.error("0xA0701 the type for super class cannot be resolved.",
            node.get_SourcePositionStart());
      }
    }
    for (ASTMCType superInterface : node.getImplementedInterfaceList()) {
      superInterface.accept(typeResolver);
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference interfaceType = typeResolver.getResult().get();
        if (!JavaDSLHelper.isPrimitiveType(interfaceType)) {
          Collection<JavaTypeSymbol> typeSymbols = interfaceType.getEnclosingScope()
              .resolveMany(interfaceType.getName(), JavaTypeSymbol.KIND);
          if (typeSymbols.isEmpty()) {
            Log.error(
                "0xA0706 the symbol of super interface '" + interfaceType.getName()
                    + "' is not found.",
                node.get_SourcePositionStart());
          }
          if (typeSymbols.size() > 1) {
            Log.error(
                "0xA0707 the symbol of super class '" + interfaceType.getName() + "' is ambiguous.",
                node.get_SourcePositionStart());
          }
          if (typeSymbols.size() == 1 && !interfaceType.getActualTypeArguments().isEmpty()) {
            JavaTypeSymbol interfaceSymbol = typeSymbols.iterator().next();
            if (interfaceSymbol.getFormalTypeParameters().isEmpty()) {
              Log.error("0xA0708 the interface '" + interfaceType.getName() + "' is not generic.",
                  node.get_SourcePositionStart());
            }
            else if (interfaceSymbol.getFormalTypeParameters().size() != interfaceType
                .getActualTypeArguments().size()) {
              Log.error("0xA0709 incorrect number of type arguments for the type '");
            }
          }
        }
      }
      else {
        Log.error("0xA0708 the type for super interface cannot be resolved.",
            node.get_SourcePositionStart());
      }
    }
  }
}
