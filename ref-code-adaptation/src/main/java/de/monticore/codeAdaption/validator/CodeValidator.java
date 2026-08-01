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
import java.util.function.Supplier;

/**
 * Checks whether handwritten Java code can be adapted to a reference class diagram. It also
 * provides the mappings that are later used to update the Java code.
 *
 * <p>The validation has three parts:
 *
 * <ul>
 *   <li>{@link ValidAnnotation} checks each relevant {@code @Adapt} annotation. Its template must
 *       exist, the number of placeholders and references must be equal, and every reference must
 *       exist in the reference CD.
 *   <li>{@link OneVarInDeclaration} rejects declarations such as {@code String first, second;}.
 *       The updater can only process one declared variable at a time.
 *   <li>Every relevant Java type, field, method, parameter, and local variable is matched to the
 *       reference CD. The {@link AdapterParam} settings decide whether an element without a match
 *       is allowed or reported as an error.
 * </ul>
 *
 * <p>This class does not compile Java code, run all JavaDSL or CD4Code checks, or check CD
 * conformance. Other parts of the application perform those tasks.
 */
public class CodeValidator {
  /** Defines what happens when a Java declaration has no match in the reference CD. */
  public enum ValidationPolicy {
    /** Use the {@code IGNORE_NON_MATCHED_*} settings from the supplied {@link AdapterParam}s. */
    CONFIGURED,
    /**
     * Allow additional Java types, members, and variables that belong to an already selected
     * concretization helper set. The usual annotation and declaration checks still run. If a
     * declaration does have a CD match, that match is checked normally.
     */
    PRESERVE_CONCRETIZATION_HELPERS
  }

  private final CompTypeMatcher typeMatcher;
  private final CompTMemberMatcher tMemberMatcher;
  /**
   * Checks supertypes with the configured strictness, even when other unmatched helper
   * declarations are allowed.
   */
  private final CompTMemberMatcher validationSupertypeMatcher;
  private final CompVariableMatcher variableMatcher;

  /**
   * Creates the matcher chains in configured precedence order.
   *
   * @param cd reference class diagram used to resolve Java elements
   * @param params enabled matching and unmatched-element policies
   */
  public CodeValidator(ASTCDCompilationUnit cd, Set<AdapterParam> params) {
    this(cd, params, ValidationPolicy.CONFIGURED);
  }

  /**
   * Creates the matchers and specifies how Java declarations without a CD match are handled.
   *
   * <p>The supplied parameter set is not changed. {@link
   * ValidationPolicy#PRESERVE_CONCRETIZATION_HELPERS} may only be used after the required helper
   * files have already been selected. Manual adaptation must use {@link
   * ValidationPolicy#CONFIGURED}.
   *
   * @param cd reference class diagram used to resolve Java elements
   * @param params enabled matching and unmatched-element settings
   * @param validationPolicy treatment of Java declarations without a CD match
   */
  public CodeValidator(
      ASTCDCompilationUnit cd, Set<AdapterParam> params, ValidationPolicy validationPolicy) {
    Objects.requireNonNull(params, "params");
    Objects.requireNonNull(validationPolicy, "validationPolicy");
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

    // Helper mode may allow additional members, but it must still reject unknown external
    // supertypes. Therefore, supertypes use a separate matcher with the configured strictness.
    List<TMemberMatcher> configuredSupertypeMatchers = new ArrayList<>(tMemberMatchers);
    if (params.contains(IGNORE_NON_MATCHED_TYPE_MEMBER)) {
      configuredSupertypeMatchers.add(new IgnoreTMemberMatcher());
    } else {
      configuredSupertypeMatchers.add(new ErrorTMemberMatcher());
    }

    boolean preserveHelpers =
        validationPolicy == ValidationPolicy.PRESERVE_CONCRETIZATION_HELPERS;
    if (preserveHelpers || params.contains(IGNORE_NON_MATCHED_TYPE)) {
      typeMatchers.add(new IgnoreTypeMatcher());
    } else {
      typeMatchers.add(new ErrorTypeMatcher());
    }

    if (preserveHelpers || params.contains(IGNORE_NON_MATCHED_TYPE_MEMBER)) {
      tMemberMatchers.add(new IgnoreTMemberMatcher());
    } else {
      tMemberMatchers.add(new ErrorTMemberMatcher());
    }

    if (preserveHelpers || params.contains(IGNORE_NON_MATCHED_VAR)) {
      variableMatchers.add(new IgnoreVariableMatcher());
    } else {
      variableMatchers.add(new ErrorVariableMatcher());
    }

    typeMatcher = new CompTypeMatcher(typeMatchers);
    variableMatcher = new CompVariableMatcher(variableMatchers);
    tMemberMatcher = new CompTMemberMatcher(tMemberMatchers);
    validationSupertypeMatcher = new CompTMemberMatcher(configuredSupertypeMatchers);

    tMemberMatcher.setTypeMatcher(typeMatcher);
    validationSupertypeMatcher.setTypeMatcher(typeMatcher);
  }

  /**
   * Parses all reference Java files, runs the adapter CoCos, and verifies matcher coverage.
   *
   * @param refCD reference CD used by annotation validation
   * @param refCode directory containing handwritten reference Java
   * @return {@code true} if this validation run produced no new error findings
   */
  public boolean isValid(ASTCDCompilationUnit refCD, Path refCode) {
    Objects.requireNonNull(refCode, "refCode");
    return validate(refCD, () -> readJavaCode(refCode), null);
  }

  /**
   * Validates Java files that have already been parsed.
   *
   * <p>This avoids parsing the same files again after helper dependency selection. The supplied
   * ASTs are read but not modified.
   *
   * @param refCD reference CD used by annotation validation
   * @param asts pre-parsed Java compilation units to validate
   * @return {@code true} if this validation run produced no new error findings
   */
  public boolean isValid(
      ASTCDCompilationUnit refCD, Set<ASTOrdinaryCompilationUnit> asts) {
    Objects.requireNonNull(asts, "asts");
    return validate(refCD, () -> asts, null);
  }

  /**
   * Validates a selected set of top-level types against the reference CD.
   *
   * <p>All supplied source files are loaded so that references to other source types can be
   * resolved. However, CD matching for types, members, variables, and supertypes is only performed
   * for {@code selectedTopLevelTypeIdentities}. Annotation and declaration-shape checks still run
   * for every supplied source file.
   *
   * @param refCD reference CD used by annotation validation
   * @param asts pre-parsed Java compilation units to validate
   * @param selectedTopLevelTypeIdentities package-qualified top-level identities selected for one
   *     concretization mapping
   * @return {@code true} if this validation run produced no new error findings
   */
  public boolean isValid(
      ASTCDCompilationUnit refCD,
      Set<ASTOrdinaryCompilationUnit> asts,
      Set<String> selectedTopLevelTypeIdentities) {
    Objects.requireNonNull(asts, "asts");
    Objects.requireNonNull(selectedTopLevelTypeIdentities, "selectedTopLevelTypeIdentities");
    return validate(refCD, () -> asts, Set.copyOf(selectedTopLevelTypeIdentities));
  }

  private boolean validate(
      ASTCDCompilationUnit refCD,
      Supplier<Set<ASTOrdinaryCompilationUnit>> sourceUnits,
      Set<String> selectedTopLevelTypeIdentities) {
    boolean failQuickEnabled = Log.isFailQuickEnabled();
    int findingsBefore = Log.getFindings().size();
    long errorsBefore = Log.getErrorCount();
    boolean valid;
    List<String> diagnostics = List.of();
    try {
      Log.enableFailQuick(false);
      Set<ASTOrdinaryCompilationUnit> asts = sourceUnits.get();

      // Run annotation and declaration-shape checks for every source file. These checks are also
      // global when CD matching below is limited to a selected helper set.
      asts.forEach(ast -> runAdapterCoCos(ast, refCD));

      // Collect every declared type before matching. This lets the matchers recognize references
      // to other supplied source files and distinguish them from unknown external types.
      Set<ASTTypeDeclaration> allTypes = new LinkedHashSet<>();
      Map<ASTOrdinaryCompilationUnit, JavaAstElemCollector> collectors = new LinkedHashMap<>();
      for (ASTOrdinaryCompilationUnit ast : asts) {
        JavaAstElemCollector collector = new JavaAstElemCollector();
        JavaDSLTraverser traverser = JavaDSLMill.traverser();
        traverser.add4JavaDSL(collector);
        ast.accept(traverser);

        collectors.put(ast, collector);
        allTypes.addAll(collector.getAllTypeDeclarations());
      }
      typeMatcher.setAllTypeDeclarations(allTypes);

      // Check CD matches now that all source types are known. If a helper set was supplied, skip
      // top-level types that do not belong to it.
      for (Map.Entry<ASTOrdinaryCompilationUnit, JavaAstElemCollector> entry :
          collectors.entrySet()) {
        ASTOrdinaryCompilationUnit unit = entry.getKey();
        checkAllMatching(
            entry.getValue(),
            type ->
                selectedTopLevelTypeIdentities == null
                    || selectedTopLevelTypeIdentities.contains(qualifiedTypeIdentity(unit, type)),
            selectedTopLevelTypeIdentities);
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
    checkAllMatching(collector, type -> true, null);
  }

  private void checkAllMatching(
      JavaAstElemCollector collector,
      java.util.function.Predicate<ASTTypeDeclaration> validateType,
      Set<String> selectedTopLevelTypeIdentities) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      if (!validateType.test(type)) {
        continue;
      }
      if (getMatchedType(type).isPresent()) {
        collector
            .getAllFSuperTypeDeclarations(type)
            .forEach(
                supertype -> {
                  if (!isSelectedQualifiedSourceSupertype(
                      supertype, selectedTopLevelTypeIdentities)) {
                    validationSupertypeMatcher.getMatchedSupertype(type, supertype);
                  }
                });
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

  /**
   * Returns whether a qualified supertype is another source type in the selected helper set.
   *
   * <p>For example, in {@code class A extends helpers.Base}, the normal matcher would only compare
   * the name {@code Base} with the reference CD. If dependency selection has already identified
   * {@code helpers.Base} as a required source helper, it must not be reported as an unknown
   * external supertype.
   */
  private boolean isSelectedQualifiedSourceSupertype(
      ASTMCType supertype, Set<String> selectedTopLevelTypeIdentities) {
    if (selectedTopLevelTypeIdentities == null) {
      return false;
    }
    String printed = de.monticore.codeAdaption.utils.JavaSourceNames
        .printQualifiedType(supertype)
        .replace('$', '.');
    return printed.contains(".") && selectedTopLevelTypeIdentities.contains(printed);
  }

  /** Builds the package-qualified top-level type name used by dependency selection. */
  private String qualifiedTypeIdentity(
      ASTOrdinaryCompilationUnit unit, ASTTypeDeclaration type) {
    String packageName =
        unit.isPresentPackageDeclaration()
            ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
            : "";
    return packageName.isBlank() ? type.getName() : packageName + "." + type.getName();
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
    checker.addCoCo((JavaLightASTAnnotationCoCo) new ValidAnnotation(refCD));
    // implicitly also adds CoCo to JavaDSLASTLocalVariableDeclaration
    checker.addCoCo((JavaDSLASTFieldDeclarationCoCo) new OneVarInDeclaration());
    checker.checkAll(ast);
  }
}
