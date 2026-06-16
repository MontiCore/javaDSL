package de.monticore.codeAdaption.matcher;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.*;
import java.util.function.BiFunction;

public interface TMemberMatcher {
  void setReferenceCD(ASTCDCompilationUnit cd);

  void setTypeMatcher(TypeMatcher matcher);

  TypeMatcher getTypeMatcher();

  Optional<CodeMatching> getMatchedMethod(ASTTypeDeclaration type, ASTMethodDeclaration method);

  Optional<CodeMatching> getMatchedField(ASTTypeDeclaration type, ASTFieldDeclaration field);

  Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype);

  default List<ISymbol> resolveFieldReferencesOf(
      ASTTypeDeclaration type, String name, BiFunction<String, String, Boolean> match) {

    List<ISymbol> references = new ArrayList<>();

    Optional<CodeMatching> matching = getTypeMatcher().getMatchedType(type);

    if (matching.isEmpty()) {
      return references;
    }

    // resolve attribute reference in referencing types
    for (ISymbol symbol : matching.get().getReferences()) {
      for (ASTCDAttribute att : ((ASTCDType) symbol.getAstNode()).getCDAttributeList()) {
        if (match.apply(name, att.getName())) {
          references.add(att.getSymbol());
        }
      }
    }
    return references;
  }

  default List<ISymbol> resolveMethodReferencesOf(
      ASTTypeDeclaration type, String name, BiFunction<String, String, Boolean> match) {
    return resolveMethodReferencesOf(type, name, match, -1); // -1 - ignore parameter count
  }

  default List<ISymbol> resolveMethodReferencesOf(
      ASTTypeDeclaration type, String name, BiFunction<String, String, Boolean> match, int paramCount) {

    List<ISymbol> references = new ArrayList<>();
    Optional<CodeMatching> matching = getTypeMatcher().getMatchedType(type);

    if (matching.isEmpty()) {
      return references;
    }

    // resolve method reference in referencing types
    for (ISymbol symbol : matching.get().getReferences()) {
      for (ASTCDMethod cdMethod : ((ASTCDType) symbol.getAstNode()).getCDMethodList()) {
        if (match.apply(name, cdMethod.getName())) {
          // If paramCount is specified, also check parameter count for overloaded methods
          if (paramCount >= 0 && cdMethod.getCDParameterList().size() != paramCount) {
            continue; // Skip methods with different parameter counts
          }
          references.add(cdMethod.getSymbol());
        }
      }
    }
    return references;
  }

  default List<ISymbol> resolveTypeReferencesOf(
      ASTTypeDeclaration type, String name, BiFunction<String, String, Boolean> match) {

    List<ISymbol> references = new ArrayList<>();
    Optional<CodeMatching> matching = getTypeMatcher().getMatchedType(type);

    if (matching.isEmpty()) {
      return references;
    }

    // resolve attribute reference in referencing types
    for (ISymbol symbol : matching.get().getReferences()) {

      if (match.apply(name, symbol.getName())) {
        references.add(symbol);
      }
    }
    return references;
  }
}
