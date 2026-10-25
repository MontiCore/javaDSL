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

/** Strategy for owner-aware matching of Java fields, methods, and supertype references. */
public interface TMemberMatcher {
  /** Sets the reference CD used by subsequent matching requests. */
  void setReferenceCD(ASTCDCompilationUnit cd);

  /** Sets the type matcher used to resolve the reference owner of a Java member. */
  void setTypeMatcher(TypeMatcher matcher);

  /** Returns the type matcher that establishes member ownership. */
  TypeMatcher getTypeMatcher();

  /** Matches a Java method beneath its Java/reference owner. */
  Optional<CodeMatching> getMatchedMethod(ASTTypeDeclaration type, ASTMethodDeclaration method);

  /** Matches a Java field beneath its Java/reference owner. */
  Optional<CodeMatching> getMatchedField(ASTTypeDeclaration type, ASTFieldDeclaration field);

  /** Matches a declared Java superclass or interface reference. */
  Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype);

  /** Resolves matching CD attributes from every reference type represented by the Java owner. */
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

  /** Resolves matching CD methods without restricting overload arity. */
  default List<ISymbol> resolveMethodReferencesOf(
      ASTTypeDeclaration type, String name, BiFunction<String, String, Boolean> match) {
    return resolveMethodReferencesOf(type, name, match, -1); // -1 - ignore parameter count
  }

  /**
   * Resolves matching CD methods, optionally requiring an exact parameter count when {@code
   * paramCount >= 0}.
   */
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

  /** Resolves matched reference types represented directly by the Java owner. */
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
