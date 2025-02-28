package de.monticore.codeAdaption.matcher.ignoreMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.util.Optional;
import java.util.Set;

/***
 *Return an empty-matching with the ignore flag set as true.
 *When the previous strategy didn't find machining for a type.
 *The adaption will ignore the corresponding element.
 */
public class IgnoreTypeMatcher implements TypeMatcher {
  protected ASTCDCompilationUnit cd;
  Set<ASTTypeDeclaration> typeDeclarationSet;

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public Set<ASTTypeDeclaration> getAllTypeDeclarations() {
    return typeDeclarationSet;
  }

  @Override
  public void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarationSet) {
    this.typeDeclarationSet = typeDeclarationSet;
  }

  @Override
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type) {
    return Optional.of(new CodeMatching(true));
  }
}
