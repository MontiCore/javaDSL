package de.monticore.codeAdaption.matcher.errorMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import java.util.Set;

/**
 * Terminal fallback that reports an unmatched Java type as a validation error.
 * It is installed when {@code IGNORE_NON_MATCHED_TYPE} is not enabled.
 */
public class ErrorTypeMatcher implements TypeMatcher {
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
    String pos = AdapterUtils.getPosition(type.get_SourcePositionStart());
    Log.error(pos + " No Match found for the type " + type.getName());
    assert false;
    return Optional.empty();
  }
}
