/* (c) https://github.com/MontiCore/monticore */

package de.monticore.java.cocos.names;

import java.util.Collection;

import de.monticore.java.javadsl._ast.ASTInterfaceDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTInterfaceDeclarationCoCo;
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
public class InterfaceValidSuperTypes implements JavaDSLASTInterfaceDeclarationCoCo {
  
  HCJavaDSLTypeResolver typeResolver;
  
  public InterfaceValidSuperTypes(HCJavaDSLTypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }
  
  @Override
  public void check(ASTInterfaceDeclaration node) {
    for (ASTMCType extendedInterface : node.getExtendedInterfaceList()) {
      extendedInterface.accept(typeResolver);
      if (typeResolver.getResult().isPresent()) {
        JavaTypeSymbolReference extendedType = typeResolver.getResult().get();
        if (!JavaDSLHelper.isPrimitiveType(extendedType)) {
          Collection<JavaTypeSymbol> typeSymbols = extendedType.getEnclosingScope()
              .resolveMany(extendedType.getName(), JavaTypeSymbol.KIND);
          if (typeSymbols.isEmpty()) {
            Log.error("0xA0709 the symbol of extended interface '" + extendedType.getName()
                + "' is not found.",
                node.get_SourcePositionStart());
          }
          if (typeSymbols.size() > 1) {
            Log.error("0xA0710 the symbol of extended interface '" + extendedType.getName()
                + "' is ambiguous.",
                node.get_SourcePositionStart());
          }
          if (typeSymbols.size() == 1 && !extendedType.getActualTypeArguments().isEmpty()) {
            JavaTypeSymbol interfaceSymbol = typeSymbols.iterator().next();
            if (interfaceSymbol.getFormalTypeParameters().isEmpty()) {
              Log.error("0xA0711 the interface '" + extendedType.getName()
                  + "' is not generic. Cannot be"
                  + "parametrized.", node.get_SourcePositionStart());
            }
            if (interfaceSymbol.getFormalTypeParameters().size() != extendedType
                .getActualTypeArguments()
                .size()) {
              Log.error("0xA0712 incorrect number of type arguments for the type '"
                  + extendedType.getName() + "'.", node.get_SourcePositionStart());
            }
          }
        }
      }
      else {
        Log.error("0xA0713 the type for super class cannot be resolved.",
            node.get_SourcePositionStart());
      }
    }
  }
}
