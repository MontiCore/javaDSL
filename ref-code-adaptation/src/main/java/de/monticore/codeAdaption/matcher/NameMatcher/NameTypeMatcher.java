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

/***
 * match a type in the code with a type with a type with the same name
 * in the reference class diagram.
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

  /***
   * match a type to a type with the same name in the class diagram.
   */
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
