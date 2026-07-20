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
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._cocos.*;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.javalight._cocos.JavaLightASTAnnotationCoCo;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.nio.file.Path;
import java.util.*;

/**
 * Validates whether handwritten reference Java can be processed safely against a reference class
 * diagram and exposes the matchings used by the update phase.
 *
 * <p>Validation performs three checks:
 *
 * <ul>
 *   <li>The adapter-specific {@link ValidAnnotation} CoCo requires each non-ignored {@code @Adapt}
 *       annotation to define a template, requires its placeholder count to match its reference
 *       count, and requires every named reference to resolve in the reference CD.
 *   <li>The {@link OneVarInDeclaration} CoCo rejects field and local-variable declarations such as
 *       {@code String first, second;} because the updater addresses one declarator at a time.
 *   <li>The configured matcher chains are queried for every relevant Java type, field, method,
 *       parameter, and local variable. Depending on the {@link AdapterParam} settings, an
 *       unmatched element is either accepted by an ignore matcher or reported by an error matcher.
 * </ul>
 *
 * <p>This class does not compile the Java source, run the complete JavaDSL or CD4Code CoCo suites,
 * or check CD conformance. Those responsibilities belong to the Java compiler and the dedicated
 * conformance/concretization services.
 */
public class CodeValidator {
  private final CompTypeMatcher typeMatcher;
  private final CompTMemberMatcher tMemberMatcher;
  private final CompVariableMatcher variableMatcher;

  /**
   * Creates the matcher chains in configured precedence order.
   *
   * @param cd reference class diagram used to resolve Java elements
   * @param params enabled matching and unmatched-element policies
   */
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

  /**
   * Parses all reference Java files, runs the adapter CoCos, and verifies matcher coverage.
   *
   * @param refCD reference CD used by annotation validation
   * @param refCode directory containing handwritten reference Java
   * @return {@code true} if this validation run produced no new error findings
   */
  public boolean isValid(ASTCDCompilationUnit refCD, Path refCode) {
    boolean failQuickEnabled = Log.isFailQuickEnabled();
    int findingsBefore = Log.getFindings().size();
    long errorsBefore = Log.getErrorCount();
    boolean valid;
    List<String> diagnostics = List.of();
    try {
      Log.enableFailQuick(false);
      Set<ASTOrdinaryCompilationUnit> asts = readJavaCode(refCode);

      // Check the adapter-specific CoCos before matcher queries can add further findings.
      asts.forEach(ast -> runAdapterCoCos(ast, refCD));

      // Collect the complete source scope before any matcher query. ErrorTMemberMatcher uses this
      // scope to distinguish a valid source-local supertype from an unmatched external type.
      Set<ASTTypeDeclaration> allTypes = new LinkedHashSet<>();
      List<JavaAstElemCollector> collectors = new ArrayList<>();
      for (ASTOrdinaryCompilationUnit ast : asts) {
        JavaAstElemCollector collector = new JavaAstElemCollector();
        JavaDSLTraverser traverser = JavaDSLMill.traverser();
        traverser.add4JavaDSL(collector);
        ast.accept(traverser);

        collectors.add(collector);
        allTypes.addAll(collector.getAllTypeDeclarations());
      }
      typeMatcher.setAllTypeDeclarations(allTypes);

      // Check that every relevant element has a match only after the complete scope is available.
      for (JavaAstElemCollector collector : collectors) {
        checkAllMatching(collector);
      }
      valid = Log.getErrorCount() == errorsBefore;
      if (!valid) {
        diagnostics =
            Log.getFindings().subList(findingsBefore, Log.getFindings().size()).stream()
                .map(finding -> finding.getMsg())
                .distinct()
                .toList();
      }
    } finally {
      if (Log.getErrorCount() > errorsBefore && Log.getFindings().size() > findingsBefore) {
        Log.getFindings().subList(findingsBefore, Log.getFindings().size()).clear();
      }
      Log.enableFailQuick(failQuickEnabled);
    }
    diagnostics.forEach(message -> Log.warn("Reference-code validation: " + message));
    return valid;
  }

  /**
   * Initializes the type matcher with all declarations from the supplied compilation units.
   *
   * <p>The complete type set is needed by matchers that decide whether a type name is unique in the
   * current source batch.
   *
   * @param javaFiles Java compilation units whose type declarations form the matching scope
   */
  public void initializeTypeMatcher(Set<ASTOrdinaryCompilationUnit> javaFiles) {
    Set<ASTTypeDeclaration> allTypes = new LinkedHashSet<>();

    for (ASTOrdinaryCompilationUnit ast : javaFiles) {
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      ast.accept(traverser);

      allTypes.addAll(collector.getAllTypeDeclarations());
    }

    typeMatcher.setAllTypeDeclarations(allTypes);
  }

  /**
   * Queries every configured matcher so missing required matches are emitted before adaptation.
   * Member and variable matchers are only queried when their enclosing type or method matched.
   */
  protected void checkAllMatching(JavaAstElemCollector collector) {

    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      if (getMatchedType(type).isPresent()) {
        collector
            .getAllFSuperTypeDeclarations(type)
            .forEach(supertype -> getMatchedSupertype(type, supertype));
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

  /** Returns the configured matching for a Java method, if a matcher accepted it. */
  public Optional<CodeMatching> getMatchedMethod(
      ASTTypeDeclaration type, ASTMethodDeclaration element) {
    return tMemberMatcher.getMatchedMethod(type, element);
  }

  /** Returns the configured matching for a Java field, if a matcher accepted it. */
  public Optional<CodeMatching> getMatchedField(
      ASTTypeDeclaration type, ASTFieldDeclaration element) {
    return tMemberMatcher.getMatchedField(type, element);
  }

  /** Returns the configured matching for a declared Java supertype, if one exists. */
  public Optional<CodeMatching> getMatchedSupertype(ASTTypeDeclaration type, ASTMCType supertype) {
    return tMemberMatcher.getMatchedSupertype(type, supertype);
  }

  /** Returns the configured matching for a Java type declaration, if a matcher accepted it. */
  public Optional<CodeMatching> getMatchedType(ASTTypeDeclaration element) {
    return typeMatcher.getMatchedType(element);
  }

  /** Returns the configured matching for a local variable inside the supplied method. */
  public Optional<CodeMatching> getMatchedLocalVariable(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTLocalVariableDeclaration element) {
    return variableMatcher.getMatchedLocalVariable(type, method, element);
  }

  /** Returns the configured matching for a formal parameter inside the supplied method. */
  public Optional<CodeMatching> getMatchedParameter(
      ASTTypeDeclaration type, ASTMethodDeclaration method, ASTFormalParameter element) {
    return variableMatcher.getMatchedFormalParameter(type, method, element);
  }

  /**
   * Runs only the adapter-specific annotation and single-variable-declaration CoCos on one Java
   * compilation unit.
   */
  protected void runAdapterCoCos(ASTOrdinaryCompilationUnit ast, ASTCDCompilationUnit refCD) {
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo((JavaDSLASTJavaAnnotationCoCo) new ValidAnnotation(refCD));
    checker.addCoCo((JavaLightASTAnnotationCoCo) new ValidAnnotation(refCD));
    // implicitly also adds CoCo to JavaDSLASTLocalVariableDeclaration
    checker.addCoCo((JavaDSLASTFieldDeclarationCoCo) new OneVarInDeclaration());
    checker.checkAll(ast);
  }
}
