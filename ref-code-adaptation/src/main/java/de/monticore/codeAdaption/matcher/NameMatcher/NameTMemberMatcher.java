package de.monticore.codeAdaption.matcher.NameMatcher;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.utils.AdapterUtils;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/***
 * match Field and method in the code with attribute and method in the class diagram
 * having the same name.
 */
public class NameTMemberMatcher implements TMemberMatcher {
  protected TypeMatcher typeMatcher;
  protected ASTCDCompilationUnit cd;
  private List<ASTCDType> referenceTypes;

  public NameTMemberMatcher(ASTCDCompilationUnit cd) {
    setReferenceCD(cd);
  }

  @Override
  public void setReferenceCD(ASTCDCompilationUnit cd) {
    this.cd = cd;
    this.referenceTypes = List.copyOf(AdapterUtils.getAllCDTypes(cd));
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
    List<String> parameterTypes = getMethodParameterTypes(method);
    List<ISymbol> references = resolveMethodReferencesBySignature(type, method.getName(), parameterTypes);

    if (references.size() == 1) {
      return Optional.of(MatcherHelper.mkMatching("${}", references));
    }

    return Optional.empty();
  }

  private List<String> getMethodParameterTypes(ASTMethodDeclaration method) {
    if (!method.getFormalParameters().isPresentFormalParameterListing()) {
      return new ArrayList<>();
    }
    return method.getFormalParameters().getFormalParameterListing().getFormalParameterList().stream()
        .map(p -> JavaSourceNames.normalizeType(JavaLoader.print(p.getMCType())))
        .collect(Collectors.toList());
  }

  private List<ISymbol> resolveMethodReferencesBySignature(
      ASTTypeDeclaration type, String methodName, List<String> parameterTypes) {

    List<ISymbol> references = new ArrayList<>();
    Optional<CodeMatching> matching = getTypeMatcher().getMatchedType(type);

    if (matching.isEmpty()) {
      return references;
    }

    for (ISymbol symbol : matching.get().getReferences()) {
      if (symbol.getAstNode() instanceof ASTCDType) {
        ASTCDType cdType = (ASTCDType) symbol.getAstNode();
        for (ASTCDMethod cdMethod : cdType.getCDMethodList()) {
          if (cdMethod.getName().equals(methodName)) {
            List<String> cdParameterTypes = cdMethod.getCDParameterList().stream()
                .map(ASTCDParameter::getMCType)
                .map(JavaLoader::print)
                .map(JavaSourceNames::normalizeType)
                .collect(Collectors.toList());

            if (parameterTypes.equals(cdParameterTypes)) {
              references.add(cdMethod.getSymbol());
            }
          }
        }
      }
    }
    return references;
  }

  private int getMethodParameterCount(ASTMethodDeclaration method) {
    if (!method.getFormalParameters().isPresentFormalParameterListing()) {
      return 0;
    }
    return method.getFormalParameters().getFormalParameterListing().getFormalParameterList().size();
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
    for (ASTCDType astcdType : referenceTypes) {
      if (srcName.toLowerCase().contains(astcdType.getName().toLowerCase())) {
        references.add(astcdType.getSymbol());
      }
    }
    // check reference and build matching
    return MatcherHelper.mkMatchingFromInfixRef(references, srcName);
  }
}
