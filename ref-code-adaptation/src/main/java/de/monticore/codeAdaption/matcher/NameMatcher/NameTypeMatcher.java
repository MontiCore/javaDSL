package de.monticore.codeAdaption.matcher.NameMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Matches a handwritten type to the reference-CD type having the same case-sensitive simple name.
 * For example, Java type {@code Entity} matches CD type {@code Entity}, while {@code
 * EntityRepository} is left to the infix strategy.
 */
public class NameTypeMatcher implements TypeMatcher {
  protected ASTCDCompilationUnit cd;
  private List<ASTCDType> referenceTypes;

  protected Set<ASTTypeDeclaration> typeDeclarationSet;

  @Override
  public Set<ASTTypeDeclaration> getAllTypeDeclarations() {
    return typeDeclarationSet;
  }

  @Override
  public void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarationSet) {
    this.typeDeclarationSet = typeDeclarationSet;
  }

  public NameTypeMatcher(ASTCDCompilationUnit cd) {
    setReferenceCD(cd);
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
    this.referenceTypes = List.copyOf(AdapterUtils.getAllCDTypes(cd));
  }

  /** Returns an identity-template matching for one equal Java/CD type name. */
  @Override
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration element) {
    for (ASTCDType type : referenceTypes) {
      if (element.getName().equals(type.getName())) {
        return Optional.of(MatcherHelper.mkMatching("${}", List.of(type.getSymbol())));
      }
    }
    return Optional.empty();
  }
}
