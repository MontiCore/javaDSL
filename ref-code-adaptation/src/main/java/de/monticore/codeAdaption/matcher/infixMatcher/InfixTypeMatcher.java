package de.monticore.codeAdaption.matcher.infixMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.symboltable.ISymbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Matches a handwritten type when its name contains one or more reference-CD type names,
 * case-insensitively.
 *
 * <p>For example, {@code EntityRepository} can reference {@code Entity}. When candidate names
 * overlap, {@link MatcherHelper} gives longer matches precedence while building the template.
 */
public class InfixTypeMatcher implements TypeMatcher {
  protected ASTCDCompilationUnit cd;

  protected Set<ASTTypeDeclaration> typeDeclarationSet;

  @Override
  public Set<ASTTypeDeclaration> getAllTypeDeclarations() {
    return typeDeclarationSet;
  }

  @Override
  public void setAllTypeDeclarations(Set<ASTTypeDeclaration> typeDeclarationSet) {
    this.typeDeclarationSet = typeDeclarationSet;
  }

  public InfixTypeMatcher(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  /** Returns an infix-derived matching for all reference-CD type names found in the Java name. */
  @Override
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration type) {
    List<ISymbol> references = new ArrayList<>();

    // collect type reference in the class diagram
    for (ASTCDType astcdType : AdapterUtils.getAllCDTypes(cd)) {
      if (type.getName().toLowerCase().contains(astcdType.getName().toLowerCase())) {
        references.add(astcdType.getSymbol());
      }
    }
    // check reference and build matching
    return MatcherHelper.mkMatchingFromInfixRef(references, type.getName());
  }

  public boolean match(String element, String infix) {
    return element.toLowerCase().contains(infix.toLowerCase());
  }
}
