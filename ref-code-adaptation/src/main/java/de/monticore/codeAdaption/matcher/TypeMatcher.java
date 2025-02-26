package de.monticore.codeAdaption.matcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl._ast.*;
import java.util.Optional;
import java.util.Set;

/***
 * this interface validates and cleans a reference code for a given reference model.
 * it also collects the necessary update operation for an adaptation.
 */

public interface TypeMatcher {
  void setReferenceCD(ASTCDCompilationUnit cd);

  Set<ASTTypeDeclaration> getAllTypeDeclarations();

  void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarations);

  Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type);
}
