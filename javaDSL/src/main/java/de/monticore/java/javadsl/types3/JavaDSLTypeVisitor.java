package de.monticore.java.javadsl.types3;

import de.monticore.java.javadsl._ast.ASTAnnotatedName;
import de.monticore.java.javadsl._ast.ASTMCQualifiedType;
import de.monticore.java.javadsl._visitor.JavaDSLVisitor2;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.util.*;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaDSLTypeVisitor extends AbstractTypeVisitor implements JavaDSLVisitor2 {
  
  @Override
  public void endVisit(ASTMCQualifiedType mcQType) {
    // adapted from endVisit(ASTMCQualifiedName qName) in MCBasicTypesTypeVisitor
    IBasicSymbolsScope enclosingScope =
        getAsBasicSymbolsScope(mcQType.getEnclosingScope());
    int numberOfPartsUsedForFirstType = 0;
    Optional<SymTypeExpression> type = Optional.empty();
    List<String> parts = mcQType.getAnnotatedNameList().stream().map(ASTAnnotatedName::getName).collect(Collectors.toList());
    do {
      numberOfPartsUsedForFirstType++;
      if (type.isEmpty()) {
        String prefix = parts.stream()
            .limit(numberOfPartsUsedForFirstType)
            .collect(Collectors.joining("."));
        type = OOWithinScopeBasicSymbolsResolver
            .resolveType(enclosingScope, prefix);
      }
      else {
        SymTypeExpression prefixType = type.get();
        String name = parts.stream()
            .skip(numberOfPartsUsedForFirstType - 1)
            .findFirst().get();
        if (prefixType.isObjectType() || prefixType.isGenericType()) {
          type = OOWithinTypeBasicSymbolsResolver.resolveType(
              prefixType,
              name,
              TypeContextCalculator.getAccessModifier(
                  prefixType.getTypeInfo(),
                  enclosingScope,
                  false
              ),
              t -> true
          );
          if (type.isEmpty()) {
            Log.error("0x7A006 unable to find type "
                    + name
                    + " within type "
                    + prefixType.printFullName(),
                mcQType.get_SourcePositionStart(),
                mcQType.get_SourcePositionEnd()
            );
          }
        }
        else {
          Log.error("0x7A007 unexpected access \"."
                  + name
                  + "\" for type "
                  + prefixType.printFullName(),
              mcQType.get_SourcePositionStart(),
              mcQType.get_SourcePositionEnd()
          );
        }
      }
      
    } while (numberOfPartsUsedForFirstType < mcQType.sizeAnnotatedNames());
    
    if (type.isEmpty()) {
      Log.error("0x7A008 Cannot find symbol " + mcQType.getAnnotatedNameList().stream().map(ASTAnnotatedName::getName).collect(Collectors.joining(".")),
          mcQType.get_SourcePositionStart(),
          mcQType.get_SourcePositionEnd()
      );
      type = Optional.of(SymTypeExpressionFactory.createObscureType());
    }
    
    getType4Ast().setTypeOfTypeIdentifier(mcQType, type.get());
  }
}
