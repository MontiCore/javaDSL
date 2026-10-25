package de.monticore.codeAdaption.matcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl._ast.*;
import java.util.Optional;
import java.util.Set;

/** Strategy for mapping Java type declarations to elements of one reference class diagram. */
public interface TypeMatcher {
  /** Sets the reference CD used by subsequent matching requests. */
  void setReferenceCD(ASTCDCompilationUnit cd);

  /** Returns all Java type declarations currently visible to cross-type matching. */
  Set<ASTTypeDeclaration> getAllTypeDeclarations();

  /** Replaces the Java type declarations visible to cross-type matching. */
  void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarations);

  /**
   * Matches one Java type.
   *
   * @return a matching (including explicit ignore results), or empty when this strategy does not
   *     apply
   */
  Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type);
}
