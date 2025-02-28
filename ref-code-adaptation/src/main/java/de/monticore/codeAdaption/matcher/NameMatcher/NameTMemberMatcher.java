package de.monticore.codeAdaption.matcher.NameMatcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/***
 * match Field and method in the code with attribute and method in the class diagram
 * having the same name.
 */
public class NameTMemberMatcher implements TMemberMatcher {
  protected TypeMatcher typeMatcher;
  protected ASTCDCompilationUnit cd;

  public NameTMemberMatcher(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
  }

  @Override
  public void setTypeMatcher(TypeMatcher typeMatcher) {
    this.typeMatcher = typeMatcher;
  }

  @Override
  public TypeMatcher getTypeMatcher() {
    return typeMatcher;
  }

  @Override
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration method) {
    List<ISymbol> references = resolveMethodReferencesOf(type, method.getName(), String::equals);

    if (references.size() == 1) {
      return Optional.of(MatcherHelper.mkMatching("${}", references));
    }

    return Optional.empty();
  }

  /***
   * match a field in referring classes with the same name.
   * example :field "id" in "EntityBuilder" refers to "id" in Entity.
   */
  @Override
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration field) {
    String fieldName = field.getVariableDeclarator(0).getDeclarator().getName();

    List<ISymbol> references = resolveFieldReferencesOf(type, fieldName, String::equals);
    if (references.size() == 1) {
      return Optional.of(MatcherHelper.mkMatching("${}", references));
    }

    return Optional.empty();
  }

  @Override
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    List<ISymbol> references = new ArrayList<>();
    String srcName = JavaLoader.print(supertype);
    // collect type reference in the class diagram
    for (ASTCDType astcdType : AdapterUtils.getAllCDTypes(cd)) {
      if (srcName.toLowerCase().contains(astcdType.getName().toLowerCase())) {
        references.add(astcdType.getSymbol());
      }
    }
    // check reference and build matching
    return MatcherHelper.mkMatchingFromInfixRef(references, srcName);
  }
}
