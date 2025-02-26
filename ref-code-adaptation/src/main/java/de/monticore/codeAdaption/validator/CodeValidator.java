package de.monticore.codeAdaption.validator;

import static de.monticore.codeAdaption.utils.AdapterParam.*;
import static de.monticore.codeAdaption.utils.JavaLoader.readJavaCode;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.NameMatcher.NameTMemberMatcher;
import de.monticore.codeAdaption.matcher.NameMatcher.NameTypeMatcher;
import de.monticore.codeAdaption.matcher.TMemberMatcher;
import de.monticore.codeAdaption.matcher.TypeMatcher;
import de.monticore.codeAdaption.matcher.VariableMatcher;
import de.monticore.codeAdaption.matcher.annotMatcher.AnnotTMemberMatcher;
import de.monticore.codeAdaption.matcher.annotMatcher.AnnotTypeMatcher;
import de.monticore.codeAdaption.matcher.annotMatcher.AnnotVariableMatcher;
import de.monticore.codeAdaption.matcher.compMatcher.CompTMemberMatcher;
import de.monticore.codeAdaption.matcher.compMatcher.CompTypeMatcher;
import de.monticore.codeAdaption.matcher.compMatcher.CompVariableMatcher;
import de.monticore.codeAdaption.matcher.errorMatcher.ErrorTMemberMatcher;
import de.monticore.codeAdaption.matcher.errorMatcher.ErrorTypeMatcher;
import de.monticore.codeAdaption.matcher.errorMatcher.ErrorVariableMatcher;
import de.monticore.codeAdaption.matcher.ignoreMatcher.IgnoreTMemberMatcher;
import de.monticore.codeAdaption.matcher.ignoreMatcher.IgnoreTypeMatcher;
import de.monticore.codeAdaption.matcher.ignoreMatcher.IgnoreVariableMatcher;
import de.monticore.codeAdaption.matcher.infixMatcher.InfixTMemberMatcher;
import de.monticore.codeAdaption.matcher.infixMatcher.InfixTypeMatcher;
import de.monticore.codeAdaption.matcher.infixMatcher.InfixVariableMatcher;
import de.monticore.codeAdaption.utils.AdapterParam;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.cocos.OneVarInDeclaration;
import de.monticore.codeAdaption.validator.cocos.ValidAnnotation;
import de.monticore.codeAdaption.validator.cocos.ValidTemplate;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLASTFieldDeclarationCoCo;
import de.monticore.java.javadsl._cocos.JavaDSLASTJavaAnnotationCoCo;
import de.monticore.java.javadsl._cocos.JavaDSLASTTypeDeclarationCoCo;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._cocos.JavaLightASTAnnotationCoCo;
import de.monticore.javalight._cocos.JavaLightASTMethodDeclarationCoCo;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.statements.mccommonstatements._cocos.MCCommonStatementsASTFormalParameterCoCo;
import de.monticore.statements.mcvardeclarationstatements._ast.ASTLocalVariableDeclaration;
import de.monticore.statements.mcvardeclarationstatements._cocos.MCVarDeclarationStatementsASTLocalVariableDeclarationCoCo;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.nio.file.Path;
import java.util.*;

/***
 * this class check if a reference Code is valid for the given reference code.
 */

public class CodeValidator {
  private final CompTypeMatcher typeMatcher;
  private final CompTMemberMatcher tMemberMatcher;
  private final CompVariableMatcher variableMatcher;

  public CodeValidator(ASTCDCompilationUnit cd, Set<AdapterParam> params) {
    List<TypeMatcher> typeMatchers = new ArrayList<>();
    List<TMemberMatcher> tMemberMatchers = new ArrayList<>();
    List<VariableMatcher> variableMatchers = new ArrayList<>();

    if (params.contains(AdapterParam.ANNOTATION_MATCHING)) {
      typeMatchers.add(new AnnotTypeMatcher(cd));
      tMemberMatchers.add(new AnnotTMemberMatcher(cd));
      variableMatchers.add(new AnnotVariableMatcher(cd));
    }

    if (params.contains(AdapterParam.NAME_MATCHING)) {
      typeMatchers.add(new NameTypeMatcher(cd));
      tMemberMatchers.add(new NameTMemberMatcher(cd));
    }

    if (params.contains(AdapterParam.INFIX_MATCHING)) {
      typeMatchers.add(new InfixTypeMatcher(cd));
      tMemberMatchers.add(new InfixTMemberMatcher(cd));
      variableMatchers.add(new InfixVariableMatcher(cd));
    }

    if (params.contains(IGNORE_NON_MATCHED_TYPE)) {
      typeMatchers.add(new IgnoreTypeMatcher());
    } else {
      typeMatchers.add(new ErrorTypeMatcher());
    }

    if (params.contains(IGNORE_NON_MATCHED_TYPE_MEMBER)) {
      tMemberMatchers.add(new IgnoreTMemberMatcher());
    } else {
      tMemberMatchers.add(new ErrorTMemberMatcher());
    }

    if (params.contains(IGNORE_NON_MATCHED_VAR)) {
      variableMatchers.add(new IgnoreVariableMatcher());
    } else {
      variableMatchers.add(new ErrorVariableMatcher());
    }

    typeMatcher = new CompTypeMatcher(typeMatchers);
    variableMatcher = new CompVariableMatcher(variableMatchers);
    tMemberMatcher = new CompTMemberMatcher(tMemberMatchers);

    tMemberMatcher.setTypeMatcher(typeMatcher);
  }

  public boolean isValid(ASTCDCompilationUnit refCD, Path refCode) {
    Set<ASTOrdinaryCompilationUnit> asts = readJavaCode(refCode);

    // check cocos phase 1
    asts.forEach(ast -> runCoCosPhase1(ast, refCD));

    // cocos phase 2
    asts.forEach(ast -> runCoCosPhase2(ast, refCD));

    // check that all elements matched
    Set<ASTTypeDeclaration> allType = new HashSet<>();
    for (ASTOrdinaryCompilationUnit ast : asts) {
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      ast.accept(traverser);

      allType.addAll(collector.getAllTypeDeclarations());
      checkAllMatching(collector);
    }

    typeMatcher.setAllTypeDeclarations(allType);
    return true;
  }

  protected void checkAllMatching(JavaAstElemCollector collector) {

    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      if (getMatchedType(type).isPresent()) {
        collector.getAllFieldDeclarations(type).forEach(f -> getMatchedField(type, f));

        for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {
          if (getMatchedMethod(type, method).isPresent()) {
            collector
                .getAllLocVariables(type, method)
                .forEach(lv -> getMatchedLocalVariable(type, method, lv));
            collector
                .getAllParameters(type, method)
                .forEach(params -> getMatchedParameter(type, method, params));
          }
        }
      }
    }
  }

  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration element) {
    return tMemberMatcher.getMatchedMethod(type, element);
  }

  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration element) {
    return tMemberMatcher.getMatchedField(type, element);
  }

  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    return tMemberMatcher.getMatchedSupertype(type, supertype);
  }

  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration element) {
    return typeMatcher.getMatchedType(element);
  }

  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration element) {
    return variableMatcher.getMatchedLocalVariable(type, method, element);
  }

  public Optional<CodeMatching> getMatchedParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter element) {
    return variableMatcher.getMatchedFormalParameter(type, method, element);
  }

  protected void runCoCosPhase1(ASTOrdinaryCompilationUnit ast, ASTCDCompilationUnit refCD) {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo((JavaDSLASTJavaAnnotationCoCo) new ValidAnnotation(refCD));
    checker.addCoCo((JavaLightASTAnnotationCoCo) new ValidAnnotation(refCD));
    checker.addCoCo((JavaDSLASTFieldDeclarationCoCo) new OneVarInDeclaration());
    checker.addCoCo(
        (MCVarDeclarationStatementsASTLocalVariableDeclarationCoCo) new OneVarInDeclaration());
    checker.checkAll(ast);
  }

  protected void runCoCosPhase2(ASTOrdinaryCompilationUnit ast, ASTCDCompilationUnit refCD) {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo((JavaDSLASTTypeDeclarationCoCo) new ValidTemplate(refCD));
    checker.addCoCo((JavaLightASTMethodDeclarationCoCo) new ValidTemplate(refCD));
    checker.addCoCo((MCCommonStatementsASTFormalParameterCoCo) new ValidTemplate(refCD));
    checker.addCoCo(
        (MCVarDeclarationStatementsASTLocalVariableDeclarationCoCo) new ValidTemplate(refCD));
    checker.checkAll(ast);
  }
}
