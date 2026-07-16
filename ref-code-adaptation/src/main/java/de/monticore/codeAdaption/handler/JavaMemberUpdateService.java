package de.monticore.codeAdaption.handler;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Translates member-level matchings into {@link CodeUpdater} operations.
 *
 * <p>The service handles local variables, parameters, fields, methods, supertypes, and Java member
 * names derived from CD association roles. It resolves model mappings through {@link
 * ConcreteSymbolResolver}; it does not orchestrate complete adaptation passes or mutate Spoon
 * directly.
 */
final class JavaMemberUpdateService {
  private final CodeValidator validator;
  private final CodeUpdater updater;
  private final CDModelIndex referenceIndex;
  private final CDModelIndex concreteIndex;
  private final ConcreteSymbolResolver symbols;

  /** Creates the member update phase from its matching, update, model, and symbol dependencies. */
  JavaMemberUpdateService(
      CodeValidator validator,
      CodeUpdater updater,
      CDModelIndex referenceIndex,
      CDModelIndex concreteIndex,
      ConcreteSymbolResolver symbols) {
    this.validator = validator;
    this.updater = updater;
    this.referenceIndex = referenceIndex;
    this.concreteIndex = concreteIndex;
    this.symbols = symbols;
  }

  /**
   * Renames matched local variables and parameters. Parameters that lack their own matching are
   * subsequently aligned with the mapped concrete CD method by position.
   */
  void handleVariableUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {
        for (ASTLocalVariableDeclaration variable : collector.getAllLocVariables(type, method)) {
          Optional<CodeMatching> matching =
              validator.getMatchedLocalVariable(type, method, variable);
          if (matching.isPresent() && matching.get().mustBePerform()) {
            updater.updateLocalVariable(
                type, method, variable, symbols.buildConcreteName(matching.get()));
          }
        }

        Set<ASTFormalParameter> updated =
            Collections.newSetFromMap(new IdentityHashMap<>());
        for (ASTFormalParameter parameter : collector.getAllParameters(type, method)) {
          Optional<CodeMatching> matching =
              validator.getMatchedParameter(type, method, parameter);
          if (matching.isPresent() && matching.get().mustBePerform()) {
            updater.updateMethodParameter(
                type, method, parameter, symbols.buildConcreteName(matching.get()));
            updated.add(parameter);
          }
        }
        updateMethodParametersFromConcreteCD(type, method, collector, updated);
      }
    }
  }

  /**
   * Copies still-unmodified parameter names from the mapped concrete CD method. An explicit
   * parameter matching always wins and is therefore listed in {@code alreadyUpdated}.
   */
  void updateMethodParametersFromConcreteCD(
      ASTTypeDeclaration type,
      ASTMethodDeclaration method,
      JavaAstElemCollector collector,
      Set<ASTFormalParameter> alreadyUpdated) {
    List<ASTFormalParameter> referenceParameters = collector.getAllParameters(type, method);
    if (referenceParameters.isEmpty()
        || referenceParameters.stream().allMatch(alreadyUpdated::contains)) {
      return;
    }
    Optional<CodeMatching> matching = validator.getMatchedMethod(type, method);
    if (matching.isEmpty() || !matching.get().mustBePerform()) {
      return;
    }

    Optional<ASTCDMethod> concreteMethod =
        symbols
            .resolveConcreteMethodSymbol(matching.get())
            .filter(symbol -> symbol.getAstNode() instanceof ASTCDMethod)
            .map(symbol -> (ASTCDMethod) symbol.getAstNode());
    if (concreteMethod.isEmpty()) {
      String concreteOwner = symbols.resolveConcreteTypeName(type.getName());
      String concreteName = symbols.buildConcreteName(matching.get());
      concreteMethod =
          symbols.findConcreteMethod(concreteOwner, concreteName, referenceParameters);
    }
    if (concreteMethod.isEmpty()) {
      return;
    }

    List<ASTCDParameter> concreteParameters = concreteMethod.get().getCDParameterList();
    for (int i = 0; i < referenceParameters.size() && i < concreteParameters.size(); i++) {
      ASTFormalParameter referenceParameter = referenceParameters.get(i);
      if (!alreadyUpdated.contains(referenceParameter)) {
        updater.updateMethodParameter(
            type, method, referenceParameter, concreteParameters.get(i).getName());
      }
    }
  }

  /**
   * Updates methods, fields, and supertypes of ordinary mapped types. Types carrying a generation
   * template are skipped because their member lifecycle is handled by generation.
   */
  void handleMemberUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      Optional<CodeMatching> typeMatching = validator.getMatchedType(type);
      if (typeMatching.isPresent()
          && typeMatching.get().getGenerateTemplate() != null
          && !typeMatching.get().getGenerateTemplate().isEmpty()) {
        continue;
      }
      updateMethods(type, collector);
      updateFields(type, collector);
      updateSupertypes(type, collector);
    }
  }

  /**
   * Renames methods whose signature still fits the concrete declaration and replaces methods whose
   * concrete parameter count differs. Methods used as {@code forEach} templates are not rewritten
   * as ordinary one-to-one methods.
   */
  private void updateMethods(ASTTypeDeclaration type, JavaAstElemCollector collector) {
    for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {
      Optional<CodeMatching> matching = validator.getMatchedMethod(type, method);
      if (matching.isEmpty() || !matching.get().mustBePerform()) {
        continue;
      }
      if (matching.get().getReferences().stream().anyMatch(this::isForEachTargetMethod)) {
        continue;
      }

      boolean hasMethodReference = false;
      Optional<ISymbol> concreteMethod = Optional.empty();
      ISymbol referenceMethod = null;
      for (ISymbol reference : matching.get().getReferences()) {
        if (isMethodReference(reference)) {
          hasMethodReference = true;
          Optional<ISymbol> resolved = resolveMappedMethod(reference);
          if (resolved.isPresent()) {
            referenceMethod = reference;
            concreteMethod = resolved;
            break;
          }
        }
      }

      if (concreteMethod.isPresent()) {
        symbols.registerConcreteMethodSignature(referenceMethod, concreteMethod.get());
        ASTCDMethod concreteDeclaration = (ASTCDMethod) concreteMethod.get().getAstNode();
        List<ASTFormalParameter> sourceParameters = collector.getAllParameters(type, method);
        if (sourceParameters.size() != concreteDeclaration.getCDParameterList().size()) {
          updater.addMethod(
              type,
              method,
              concreteDeclaration.getName(),
              concreteDeclaration.getCDParameterList().stream()
                  .map(
                      parameter ->
                          symbols.resolveConcreteCdType(
                              JavaSourceNames.printNormalizedType(parameter.getMCType())))
                  .toList(),
              concreteDeclaration.getCDParameterList().stream()
                  .map(ASTCDParameter::getName)
                  .toList(),
              symbols.resolveConcreteCdType(
                  JavaSourceNames.printNormalizedReturnType(concreteDeclaration)),
              concreteDeclaration.getModifier().isStatic(),
              MethodBodySpec.empty());
          updater.removeMethod(type, method);
        } else if (!concreteMethod.get().getName().equals(referenceMethod.getName())) {
          updater.updateMethod(type, method, concreteMethod.get().getName());
        }
      } else if (!hasMethodReference) {
        updater.updateMethod(type, method, symbols.buildConcreteName(matching.get()));
      }
    }
  }

  /** Resolves a reference method first from the incarnation context and then from conformance. */
  private Optional<ISymbol> resolveMappedMethod(ISymbol reference) {
    Optional<ISymbol> fromContext = symbols.getSymbolFromContext(reference);
    if (fromContext.isPresent() && fromContext.get().getAstNode() instanceof ASTCDMethod) {
      return fromContext;
    }
    ISymbol fromChecker = symbols.getConMethodSymbol(reference);
    if (fromChecker != reference && fromChecker.getAstNode() instanceof ASTCDMethod) {
      return Optional.of(fromChecker);
    }
    return Optional.empty();
  }

  /** Distinguishes method-like symbols from CD type and field symbols in matcher references. */
  private boolean isMethodReference(ISymbol reference) {
    return reference.getAstNode() instanceof ASTCDMethod
        || (!(reference instanceof CDTypeSymbol) && !(reference instanceof FieldSymbol));
  }

  /** Renames every matched field of one Java type. */
  private void updateFields(ASTTypeDeclaration type, JavaAstElemCollector collector) {
    for (ASTFieldDeclaration field : collector.getAllFieldDeclarations(type)) {
      Optional<CodeMatching> matching = validator.getMatchedField(type, field);
      if (matching.isPresent() && matching.get().mustBePerform()) {
        updater.updateField(type, field, symbols.buildConcreteName(matching.get()));
      }
    }
  }

  /** Rewrites every matched superclass or implemented interface of one Java type. */
  private void updateSupertypes(ASTTypeDeclaration type, JavaAstElemCollector collector) {
    for (ASTMCType supertype : collector.getAllFSuperTypeDeclarations(type)) {
      Optional<CodeMatching> matching = validator.getMatchedSupertype(type, supertype);
      if (matching.isPresent() && matching.get().mustBePerform()) {
        updater.updateSuperType(type, supertype, symbols.buildConcreteName(matching.get()));
      }
    }
  }

  /**
   * Rewrites Java names derived from CD association roles. For each adapted reference owner/target
   * pair, the method finds the corresponding role in the concrete CD and rejects ambiguous role
   * names instead of choosing one arbitrarily.
   */
  void handleAssociationRoleUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration javaType : collector.getAllTypeDeclarations()) {
      Map<String, String> rewrites = new LinkedHashMap<>();
      for (ASTCDAssociation referenceAssociation : referenceIndex.associations()) {
        for (AssociationRole referenceRole : associationRoles(referenceAssociation)) {
          if (!adaptsReferenceType(javaType, referenceRole.ownerType())) {
            continue;
          }
          String concreteOwner = resolveConcreteAssociationType(referenceRole.ownerType());
          String concreteTarget = resolveConcreteAssociationType(referenceRole.targetType());
          Set<String> concreteRoles = new LinkedHashSet<>();
          for (ASTCDAssociation concreteAssociation : concreteIndex.associations()) {
            for (AssociationRole concreteRole : associationRoles(concreteAssociation)) {
              if (concreteOwner.equals(concreteRole.ownerType())
                  && concreteTarget.equals(concreteRole.targetType())) {
                concreteRoles.add(concreteRole.roleName());
              }
            }
          }
          addRoleRewrite(rewrites, referenceRole, concreteRoles);
        }
      }
      rewrites.forEach(
          (source, concrete) -> updater.updateAssociationRole(javaType, source, concrete));
    }
  }

  /** Adds one unambiguous reference-role to concrete-role rewrite to the per-type rewrite map. */
  private void addRoleRewrite(
      Map<String, String> rewrites, AssociationRole referenceRole, Set<String> concreteRoles) {
    if (concreteRoles.size() > 1) {
      if (concreteRoles.contains(referenceRole.roleName())) {
        rewrites.putIfAbsent(referenceRole.roleName(), referenceRole.roleName());
        return;
      }
      throw new IllegalStateException(
          "Ambiguous concrete association roles for "
              + referenceRole.ownerType()
              + "."
              + referenceRole.roleName()
              + ": "
              + concreteRoles);
    }
    if (concreteRoles.size() == 1) {
      String concreteRole = concreteRoles.iterator().next();
      String previous = rewrites.putIfAbsent(referenceRole.roleName(), concreteRole);
      if (previous != null && !previous.equals(concreteRole)) {
        throw new IllegalStateException(
            "Conflicting concrete association roles for "
                + referenceRole.ownerType()
                + "."
                + referenceRole.roleName());
      }
    }
  }

  /**
   * Returns whether a referenced method is the target of another reference method's
   * {@code <<forEach="...">>} stereotype and must therefore be handled by expansion.
   */
  boolean isForEachTargetMethod(ISymbol reference) {
    if (!(reference.getAstNode() instanceof ASTCDMethod targetMethod)) {
      return false;
    }
    Optional<ASTCDType> targetOwner = referenceIndex.ownerOf(targetMethod);
    if (targetOwner.isEmpty()) {
      return false;
    }
    for (ASTCDType referenceType : referenceIndex.types()) {
      for (ASTCDMethod method : referenceType.getCDMethodList()) {
        Optional<String> target = stereotypeValue(method, "forEach");
        if (target.isPresent()
            && referencesMethod(target.get(), referenceType, targetOwner.get(), targetMethod)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Returns whether the Java declaration represents the supplied reference CD type. */
  private boolean adaptsReferenceType(ASTTypeDeclaration javaType, String referenceType) {
    Optional<CodeMatching> matching = validator.getMatchedType(javaType);
    return matching
            .map(
                value ->
                    value.getReferences().stream()
                        .filter(CDTypeSymbol.class::isInstance)
                        .map(ISymbol::getName)
                        .anyMatch(referenceType::equals))
            .orElse(false)
        || referenceType.equals(javaType.getName());
  }

  /** Resolves a reference association endpoint to its selected concrete type name. */
  private String resolveConcreteAssociationType(String referenceType) {
    Optional<ASTCDType> type = referenceIndex.type(referenceType);
    if (type.isEmpty()) {
      return referenceType;
    }
    ISymbol concrete =
        symbols
            .getSymbolFromContext(type.get().getSymbol())
            .orElseGet(() -> symbols.getConTypeSymbol(type.get().getSymbol()));
    return JavaSourceNames.simpleName(concrete.getName());
  }

  /**
   * Converts the present left and right CD roles into owner/target/name triples. A role belongs to
   * the opposite endpoint because it names navigation from that owner toward the role's endpoint.
   */
  private List<AssociationRole> associationRoles(ASTCDAssociation association) {
    String left = JavaSourceNames.simpleName(association.getLeftQualifiedName().getQName());
    String right = JavaSourceNames.simpleName(association.getRightQualifiedName().getQName());
    List<AssociationRole> roles = new ArrayList<>();
    if (association.getLeft().isPresentCDRole()) {
      roles.add(new AssociationRole(right, left, association.getLeft().getCDRole().getName()));
    }
    if (association.getRight().isPresentCDRole()) {
      roles.add(new AssociationRole(left, right, association.getRight().getCDRole().getName()));
    }
    return roles;
  }

  /**
   * Matches a {@code forEach} target expressed as either a local method name or an owner-qualified
   * method name.
   */
  private boolean referencesMethod(
      String referenceName,
      ASTCDType annotatedOwner,
      ASTCDType targetOwner,
      ASTCDMethod method) {
    String trimmed = referenceName.trim();
    if (!method.getName().equals(JavaSourceNames.simpleName(trimmed))) {
      return false;
    }
    if (!trimmed.contains(".")) {
      return annotatedOwner.getName().equals(targetOwner.getName());
    }
    String ownerName = trimmed.substring(0, trimmed.lastIndexOf('.'));
    return targetOwner.getName().equals(JavaSourceNames.simpleName(ownerName));
  }

  /** Reads one stereotype value and treats malformed generated AST values as absent. */
  private Optional<String> stereotypeValue(ASTCDMethod method, String name) {
    if (method.getModifier() == null || !method.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : method.getModifier().getStereotype().getValuesList()) {
      if (name.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (RuntimeException ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  /** Directional association role: navigation owner, target type, and generated Java name. */
  private record AssociationRole(String ownerType, String targetType, String roleName) {}
}
