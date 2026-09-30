package de.monticore.java.javadsl.types3;

import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes.types3.MCBasicTypesTypeVisitor;
import de.monticore.types3.util.TypeContextCalculator;
import de.monticore.types3.util.WithinScopeBasicSymbolsResolver;
import de.monticore.types3.util.WithinTypeBasicSymbolsResolver;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.stream.Collectors;

public class JavaDSLMCBasicTypesTypeVisitor extends MCBasicTypesTypeVisitor {
  
  @Override
  public void endVisit(ASTMCQualifiedName qName) {
    IBasicSymbolsScope enclosingScope =
        getAsBasicSymbolsScope(qName.getEnclosingScope());
    // Note: As the qualified name is a List of Names,
    // There is no ASTNode representing each prefix with a type,
    // e.g., Assume a.b.c with a being a qualifier, a.b being a type
    // and c being an inner type in a.b
    // then there is no AST node representing a.b.
    // As types are usually stored in Type4AST,
    // this cannot be done for a.b in this case.
    // The result from a.b will be discarded after this method
    
    // find the type with the smallest prefix,
    // e.g., for a.b.c.d it is a.b, if a.b and a.b.c are types,
    // with a and a.b being their qualifiers respectively.
    // Afterwards, use the prefix to search for inner types
    int numberOfPartsUsedForFirstType = 0;
    Optional<SymTypeExpression> type = Optional.empty();
    do {
      numberOfPartsUsedForFirstType++;
      if (type.isEmpty()) {
        String prefix = qName.getPartsList().stream()
            .limit(numberOfPartsUsedForFirstType)
            .collect(Collectors.joining("."));
        type = WithinScopeBasicSymbolsResolver
            .resolveType(enclosingScope, prefix);
      }
      else {
        SymTypeExpression prefixType = type.get();
        String name = qName.getParts(numberOfPartsUsedForFirstType - 1);
        if (prefixType.isObjectType() || prefixType.isGenericType()) {
          type = WithinTypeBasicSymbolsResolver.resolveType(
              prefixType,
              name,
              TypeContextCalculator.getAccessModifier(
                  prefixType.getTypeInfo(),
                  enclosingScope,
                  false // <- difference to the endVisit in the original MCBasicTypesTypeVisitor
              ),
              t -> true
          );
          if (type.isEmpty()) {
            Log.error("0x7A012 unable to find type "
                    + name
                    + " within type "
                    + prefixType.printFullName(),
                qName.get_SourcePositionStart(),
                qName.get_SourcePositionEnd()
            );
          }
        }
        else {
          Log.error("0x7A010 unexpected access \"."
                  + name
                  + "\" for type "
                  + prefixType.printFullName(),
              qName.get_SourcePositionStart(),
              qName.get_SourcePositionEnd()
          );
        }
      }
      
    } while (numberOfPartsUsedForFirstType < qName.sizeParts());
    
    if (type.isEmpty()) {
      Log.error("0x7A011 Cannot find symbol " + qName.getQName(),
          qName.get_SourcePositionStart(),
          qName.get_SourcePositionEnd()
      );
      type = Optional.of(SymTypeExpressionFactory.createObscureType());
    }
    
    getType4Ast().setTypeOfTypeIdentifier(qName, type.get());
  }
}
