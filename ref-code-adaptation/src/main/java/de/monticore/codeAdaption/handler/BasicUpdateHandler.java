package de.monticore.codeAdaption.handler;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.matcher.*;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.nio.file.Path;
import java.util.*;
import de.se_rwth.commons.logging.Log;

/** Applies CD-based adaptation decisions to Java AST elements. */
public class BasicUpdateHandler {
  protected CDModelIndex conIndex;
  protected CDModelIndex refIndex;
  protected CDConformanceChecker checker;
  protected CodeUpdater updater;

  protected CodeValidator validator;

  /** Optional incarnation context for stereotype-based mapping when conformance is skipped */
  protected IncarnationContext incarnationContext;

  protected boolean useCommonParentForMultipleIncarnations;
  protected final Map<String, String> concreteImportedTypes = new LinkedHashMap<>();

  /** Tracks generated type names created during a single handler run to avoid duplicate generation. */
  protected final Set<String> generatedTypes = new HashSet<>();

  public BasicUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator) {
    this(refCD, conCD, conHwcPath, checker, updater, validator, null);
  }

  public BasicUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationContext incarnationContext) {
    this(refCD, conCD, conHwcPath, checker, updater, validator, incarnationContext, true);
  }

  public BasicUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationContext incarnationContext,
      boolean useCommonParentForMultipleIncarnations) {
    this.updater = updater;
    this.checker = checker;
    this.conIndex = CDModelIndex.of(conCD);
    this.refIndex = CDModelIndex.of(refCD);
    this.validator = validator;
    this.incarnationContext = incarnationContext;
    this.useCommonParentForMultipleIncarnations = useCommonParentForMultipleIncarnations;
    this.concreteImportedTypes.putAll(importedTypes(conCD));
  }

  public void handleUpdate(Set<ASTOrdinaryCompilationUnit> javaFiles) {

    // Initialize TypeMatcher with collected types from the Java files
    validator.initializeTypeMatcher(javaFiles);

    // Collect elements of each type
    Set<JavaAstElemCollector> typeElements = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit ast : javaFiles) {

      // collect code elements
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      ast.accept(traverser);
      typeElements.add(collector);
    }

    // update local variable and method parameters
    typeElements.forEach(this::handleVariableUpdate);

    // update methods and attributes
    typeElements.forEach(this::handleTMemberUpdate);

    // update types
    typeElements.forEach(this::handleTypeUpdate);

    // add Java-expressible members that were introduced by cdconcretization
    typeElements.forEach(this::projectCompletedMembers);

    updater.printCode();
  }

  protected void handleTypeUpdate(JavaAstElemCollector collector) {
    // update type present in the reference code
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      Optional<CodeMatching> matching = validator.getMatchedType(type);
      if (matching.isPresent() && matching.get().mustBePerform()) {
        if (matching.get().getGenerateTemplate() != null && !matching.get().getGenerateTemplate().isEmpty()) {
          generateTypeFromTemplate(type, matching.get(), collector);
        } else {
          String newName = buildConcreteName(matching.get());
          updater.updateType(type, newName);
        }
      }
    }

    // update type not present in the reference code
    for (ASTCDType cdType : refIndex.types()) {
      String newName =
          getSymbolFromContext(cdType.getSymbol())
              .orElseGet(() -> getConTypeSymbol(cdType.getSymbol()))
              .getName();
      updater.updateCDType(cdType, newName);
    }
  }

  private void generateTypeFromTemplate(
      ASTTypeDeclaration templateType, CodeMatching typeMatching, JavaAstElemCollector collector) {
    String generatedTypeName = buildConcreteName(typeMatching);
    if (!generatedTypes.add(generatedTypeName)) {
      return;
    }

    updater.addType(templateType, generatedTypeName);

    List<ASTFieldDeclaration> templateFields = collector.getAllFieldDeclarations(templateType);
    List<ASTMethodDeclaration> templateMethods = collector.getAllMethodDeclarations(templateType);
    if (templateFields.isEmpty() || templateMethods.isEmpty()) {
      removeTemplateMembers(templateType, templateFields, templateMethods);
      return;
    }

    String concreteName = resolveGeneratedTargetTypeName(templateType, typeMatching);
    List<GeneratedAttribute> attributes = collectGeneratedAttributes(concreteName);
    ASTFieldDeclaration fieldTemplate = templateFields.get(0);
    ASTMethodDeclaration methodTemplate = templateMethods.get(0);
    List<String> buildFieldArgs = new ArrayList<>();

    for (GeneratedAttribute attribute : attributes) {
      String fieldName = attribute.name() + "Field";
      updater.addField(templateType, fieldTemplate, fieldName, attribute.type());
      buildFieldArgs.add(fieldName);

      String setterName = setterName(attribute.name());
      updater.addMethod(
          templateType,
          methodTemplate,
          setterName,
          List.of(attribute.type()),
          List.of(attribute.name()),
          generatedTypeName,
          MethodBodySpec.assignFieldAndReturnThis(fieldName, attribute.name()));
    }

    updater.addMethod(
        templateType,
        methodTemplate,
        "build",
        Collections.emptyList(),
        Collections.emptyList(),
        concreteName,
        MethodBodySpec.returnNew(concreteName, buildFieldArgs));

    removeTemplateMembers(templateType, templateFields, templateMethods);
  }

  private String resolveGeneratedTargetTypeName(ASTTypeDeclaration templateType, CodeMatching matching) {
    for (ISymbol refSym : matching.getReferences()) {
      if (refSym instanceof CDTypeSymbol cdTypeSymbol) {
        return getSymbolFromContext(refSym)
            .orElseGet(() -> getConTypeSymbol(cdTypeSymbol))
            .getName();
      }
    }
    return resolveConcreteTypeName(templateType.getName());
  }

  private List<GeneratedAttribute> collectGeneratedAttributes(String concreteName) {
    Optional<ASTCDType> concreteType = findConcreteType(concreteName);
    if (concreteType.isEmpty()) {
      return List.of();
    }

    List<GeneratedAttribute> attributes = new ArrayList<>();
    for (ASTCDAttribute attribute : concreteType.get().getCDAttributeList()) {
      String type = JavaLoader.print(attribute.getMCType());
      if (incarnationContext != null && useCommonParentForMultipleIncarnations) {
        type = replaceConcreteWithGroupingType(type);
      }
      attributes.add(new GeneratedAttribute(attribute.getName(), qualifyCdType(type)));
    }
    return attributes;
  }

  private void removeTemplateMembers(
      ASTTypeDeclaration templateType,
      List<ASTFieldDeclaration> templateFields,
      List<ASTMethodDeclaration> templateMethods) {
    for (ASTFieldDeclaration templateField : templateFields) {
      try {
        updater.removeField(templateType, templateField);
      } catch (Exception e) {
        Log.debug("Could not remove template field: " + e.getMessage(), "BasicUpdateHandler");
      }
    }
    for (ASTMethodDeclaration templateMethod : templateMethods) {
      try {
        updater.removeMethod(templateType, templateMethod);
      } catch (Exception e) {
        Log.debug("Could not remove template method: " + e.getMessage(), "BasicUpdateHandler");
      }
    }
  }

  private static String setterName(String attributeName) {
    if (attributeName == null || attributeName.isEmpty()) {
      return "set";
    }
    return "set" + JavaSourceNames.capitalize(attributeName);
  }

  protected void handleTMemberUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {

      // If the type is configured to be generated, skip member-level
      // updates here because members are created on the generated type in handleTypeUpdate.
      Optional<CodeMatching> typeMatching = validator.getMatchedType(type);
      if (typeMatching.isPresent() && typeMatching.get().getGenerateTemplate() != null
              && !typeMatching.get().getGenerateTemplate().isEmpty()) {
        continue;
      }

      // update method names
      for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {
        Optional<CodeMatching> matching = validator.getMatchedMethod(type, method);
        if (matching.isPresent() && matching.get().mustBePerform()) {
          if (matching.get().getReferences().stream().anyMatch(this::isForEachTargetMethod)) {
            continue;
          }
          // Check if this is a pattern template method with no concrete incarnation
          // If so, skip the name update to preserve the template method name
          boolean hasConcreteIncarnation = false;
          boolean hasMethodReference = false;
          String concreteMethodName = null;
          for (ISymbol ref : matching.get().getReferences()) {
            boolean methodReference =
                ref.getAstNode() instanceof ASTCDMethod
                    || (!(ref instanceof CDTypeSymbol) && !(ref instanceof FieldSymbol));
            if (methodReference) {
              hasMethodReference = true;
              Optional<ISymbol> conMethod = getSymbolFromContext(ref);
              if (conMethod.isPresent() && !conMethod.get().getName().equals(ref.getName())) {
                hasConcreteIncarnation = true;
                concreteMethodName = conMethod.get().getName();
                registerConcreteMethodSignature(ref, conMethod.get());
                break;
              }
            }
          }

          if (hasMethodReference && !hasConcreteIncarnation) {
            continue;
          }

          String newName = concreteMethodName != null ? concreteMethodName : buildConcreteName(matching.get());
          updater.updateMethod(type, method, newName);
        }
      }

      // update type parameters names
      for (ASTFieldDeclaration field : collector.getAllFieldDeclarations(type)) {
        Optional<CodeMatching> matching = validator.getMatchedField(type, field);
        if (matching.isPresent() && matching.get().mustBePerform()) {
          String newName = buildConcreteName(matching.get());
          updater.updateField(type, field, newName);
        }
      }

      // update type parameters names
      for (ASTMCType supertype : collector.getAllFSuperTypeDeclarations(type)) {
        Optional<CodeMatching> matching = validator.getMatchedSupertype(type, supertype);
        if (matching.isPresent() && matching.get().mustBePerform()) {
          String newName = buildConcreteName(matching.get());
          updater.updateSuperType(type, supertype, newName);
        }
      }
    }
  }

  private record GeneratedAttribute(String name, String type) {}

  protected void projectCompletedMembers(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      Optional<CodeMatching> typeMatching = validator.getMatchedType(type);
      if (typeMatching.isEmpty() || !typeMatching.get().mustBePerform()) {
        continue;
      }
      String concreteTypeName = buildConcreteName(typeMatching.get());
      Optional<ASTCDType> concreteType = conIndex.type(concreteTypeName);
      if (concreteType.isEmpty()) {
        continue;
      }

      for (ASTFieldDeclaration fieldTemplate : collector.getAllFieldDeclarations(type)) {
        Optional<CodeMatching> fieldMatching = validator.getMatchedField(type, fieldTemplate);
        if (fieldMatching.isEmpty() || !fieldMatching.get().mustBePerform()) {
          continue;
        }
        for (ISymbol reference : fieldMatching.get().getReferences()) {
          if (!(reference.getAstNode() instanceof ASTCDAttribute)) {
            continue;
          }
          for (ASTCDAttribute concreteAttribute : concreteAttributesFor(reference)) {
            if (!isOwnedBy(concreteAttribute, concreteType.get())) {
              continue;
            }
            updater.addField(
                type,
                fieldTemplate,
                concreteAttribute.getName(),
                qualifyCdType(JavaSourceNames.printNormalizedFieldType(concreteAttribute)),
                isStatic(concreteAttribute));
          }
        }
      }

      for (ASTMethodDeclaration methodTemplate : collector.getAllMethodDeclarations(type)) {
        Optional<CodeMatching> methodMatching = validator.getMatchedMethod(type, methodTemplate);
        if (methodMatching.isEmpty() || !methodMatching.get().mustBePerform()) {
          continue;
        }
        for (ISymbol reference : methodMatching.get().getReferences()) {
          if (!(reference.getAstNode() instanceof ASTCDMethod)) {
            continue;
          }
          if (isForEachTargetMethod(reference)) {
            continue;
          }
          for (ASTCDMethod concreteMethod : concreteMethodsFor(reference)) {
            if (!isOwnedBy(concreteMethod, concreteType.get())) {
              continue;
            }
            updater.addMethod(
                type,
                methodTemplate,
                concreteMethod.getName(),
                concreteMethod.getCDParameterList().stream()
                    .map(parameter -> qualifyCdType(JavaSourceNames.printNormalizedType(parameter.getMCType())))
                    .toList(),
                concreteMethod.getCDParameterList().stream().map(ASTCDParameter::getName).toList(),
                qualifyCdType(JavaSourceNames.printNormalizedReturnType(concreteMethod)),
                isStatic(concreteMethod));
          }
        }
      }
    }
  }

  private List<ASTCDAttribute> concreteAttributesFor(ISymbol reference) {
    List<ASTCDAttribute> result = new ArrayList<>();
    if (reference.getAstNode() instanceof ASTCDAttribute referenceAttribute
        && checker != null
        && checker.getIncarnationMapping() != null) {
      var incarnations = checker.getIncarnationMapping().getIncarnations(referenceAttribute);
      if (incarnations != null) {
        for (var incarnation : incarnations) {
          if (incarnation instanceof ASTCDAttribute attribute) {
            result.add(attribute);
          }
        }
      }
    }
    if (result.isEmpty() && incarnationContext != null) {
      List<ISymbol> incarnations = incarnationContext.getIncarnations(reference);
      if (incarnations != null) {
        for (ISymbol incarnation : incarnations) {
          if (incarnation.getAstNode() instanceof ASTCDAttribute attribute) {
            result.add(attribute);
          }
        }
      }
    }
    return result;
  }

  private List<ASTCDMethod> concreteMethodsFor(ISymbol reference) {
    List<ASTCDMethod> result = new ArrayList<>();
    if (reference.getAstNode() instanceof ASTCDMethod referenceMethod
        && checker != null
        && checker.getIncarnationMapping() != null) {
      var incarnations = checker.getIncarnationMapping().getIncarnations(referenceMethod);
      if (incarnations != null) {
        for (var incarnation : incarnations) {
          if (incarnation instanceof ASTCDMethod method) {
            result.add(method);
          }
        }
      }
    }
    if (result.isEmpty() && incarnationContext != null) {
      List<ISymbol> incarnations = incarnationContext.getIncarnations(reference);
      if (incarnations != null) {
        for (ISymbol incarnation : incarnations) {
          if (incarnation.getAstNode() instanceof ASTCDMethod method) {
            result.add(method);
          }
        }
      }
    }
    return result;
  }

  private boolean isOwnedBy(ASTCDAttribute attribute, ASTCDType owner) {
    return conIndex.ownerOf(attribute).map(type -> type == owner || type.getName().equals(owner.getName())).orElse(false);
  }

  private boolean isOwnedBy(ASTCDMethod method, ASTCDType owner) {
    return conIndex.ownerOf(method).map(type -> type == owner || type.getName().equals(owner.getName())).orElse(false);
  }

  private boolean isStatic(ASTCDAttribute attribute) {
    return attribute.getModifier().isStatic();
  }

  private boolean isStatic(ASTCDMethod method) {
    return method.getModifier().isStatic();
  }

  private boolean isForEachTargetMethod(ISymbol reference) {
    if (!(reference.getAstNode() instanceof ASTCDMethod targetMethod)) {
      return false;
    }
    Optional<ASTCDType> targetOwner = refIndex.ownerOf(targetMethod);
    if (targetOwner.isEmpty()) {
      return false;
    }
    for (ASTCDType referenceType : refIndex.types()) {
      for (ASTCDMethod method : referenceType.getCDMethodList()) {
        Optional<String> forEachTarget = getStereotypeValue(method, "forEach");
        if (forEachTarget.isPresent()
            && referencesMethod(forEachTarget.get(), referenceType, targetOwner.get(), targetMethod)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean referencesMethod(
      String referenceName, ASTCDType annotatedOwner, ASTCDType targetOwner, ASTCDMethod method) {
    String trimmed = referenceName.trim();
    String simpleName = JavaSourceNames.simpleName(trimmed);
    if (!method.getName().equals(simpleName)) {
      return false;
    }
    if (!trimmed.contains(".")) {
      return annotatedOwner.getName().equals(targetOwner.getName());
    }
    String ownerName = trimmed.substring(0, trimmed.lastIndexOf('.'));
    return targetOwner.getName().equals(JavaSourceNames.simpleName(ownerName));
  }

  private Optional<String> getStereotypeValue(ASTCDMethod method, String name) {
    if (method.getModifier() == null || !method.getModifier().isPresentStereotype()) {
      return Optional.empty();
    }
    for (var stereotype : method.getModifier().getStereotype().getValuesList()) {
      if (name.equals(stereotype.getName())) {
        try {
          return Optional.ofNullable(stereotype.getValue());
        } catch (Exception ignored) {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  protected void handleVariableUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      for (ASTMethodDeclaration method : collector.getAllMethodDeclarations(type)) {

        // update local variables
        for (ASTLocalVariableDeclaration var : collector.getAllLocVariables(type, method)) {
          Optional<CodeMatching> matching = validator.getMatchedLocalVariable(type, method, var);
          if (matching.isPresent() && matching.get().mustBePerform()) {
            String newName = buildConcreteName(matching.get());
            updater.updateLocalVariable(type, method, var, newName);
          }
        }

        // update all formal parameters using normal matching logic
        for (ASTFormalParameter param : collector.getAllParameters(type, method)) {
          Optional<CodeMatching> matching = validator.getMatchedParameter(type, method, param);
          if (matching.isPresent() && matching.get().mustBePerform()) {
            String newName = buildConcreteName(matching.get());
            updater.updateMethodParameter(type, method, param, newName);
          }
        }

        // update all formal parameters using concrete CD method lookup (fallback)
        updateMethodParametersFromConcreteCD(type, method, collector);
      }
    }
  }

  /**
   * Update method parameters by looking up the concrete method in conCD and extracting
   * parameter names directly from the concrete method signature.
   */
  protected void updateMethodParametersFromConcreteCD(
      ASTTypeDeclaration type, ASTMethodDeclaration method, JavaAstElemCollector collector) {

    List<ASTFormalParameter> refParams = collector.getAllParameters(type, method);
    if (refParams.isEmpty()) {
      return;
    }

    // Get concrete method name
    String concreteMethodName = resolveConcreteMethodName(type, method);
    if (concreteMethodName == null) {
      return;
    }

    // Find the concrete type that matches the current type
    String typeName = type.getName();
    String concreteTypeName = resolveConcreteTypeName(typeName);
    if (concreteTypeName == null) {
      return;
    }

    // Find concrete method in conCD
    Optional<ASTCDMethod> concreteMethod = findConcreteMethod(
        concreteTypeName, concreteMethodName, refParams.size());

    if (concreteMethod.isPresent()) {
      List<ASTCDParameter> conParams = concreteMethod.get().getCDParameterList();

      // Match parameters by position and update names
      for (int i = 0; i < refParams.size() && i < conParams.size(); i++) {
        String conParamName = conParams.get(i).getName();
        updater.updateMethodParameter(type, method, refParams.get(i), conParamName);
      }
    }
  }

  /**
   * Resolve concrete method name using the same logic as method renaming.
   */
  private String resolveConcreteMethodName(ASTTypeDeclaration type, ASTMethodDeclaration method) {
    Optional<CodeMatching> matching = validator.getMatchedMethod(type, method);
    if (matching.isPresent() && matching.get().mustBePerform()) {
      return buildConcreteName(matching.get());
    }
    return null;
  }

  /**
   * Resolve concrete type name using IncarnationContext or conformance checker.
   */
  private String resolveConcreteTypeName(String refTypeName) {
    // First try IncarnationContext - look up type name by matching reference type names
    if (incarnationContext != null) {
      for (Map.Entry<ISymbol, List<ISymbol>> entry : incarnationContext.getReferenceToIncarnations().entrySet()) {
        if (entry.getKey().getName().equals(refTypeName)) {
          List<ISymbol> incarnations = entry.getValue();
          if (incarnations != null && !incarnations.isEmpty()) {
            ISymbol firstInc = incarnations.get(0);
            if (useCommonParentForMultipleIncarnations) {
              var grouping = incarnationContext.findGroupingTypeForImplementer(firstInc.getName());
              if (grouping.isPresent()) {
                return grouping.get();
              }
            }
            return firstInc.getName();
          }
        }
      }
    }

    // For single mapping, use conformance checker to find the concrete type
    if (checker != null && checker.getIncarnationMapping() != null) {
      for (ASTCDType refType : refIndex.types()) {
        if (refType.getName().equals(refTypeName)) {
          var incarnations = checker.getIncarnationMapping().getIncarnations(refType);
          if (incarnations != null && incarnations.iterator().hasNext()) {
            return incarnations.iterator().next().getSymbol().getName();
          }
          break;
        }
      }
    }

    // Fall back to direct name matching in conCD (for cases where names are the same)
    // Prefer interfaces over concrete classes when both exist with the same name.
    for (ASTCDType conType : conIndex.interfaces()) {
      if (conType.getName().equals(refTypeName)) {
        return conType.getName();
      }
    }
    for (ASTCDType conType : conIndex.classes()) {
      if (conType.getName().equals(refTypeName)) {
        return conType.getName();
      }
    }

    // If not found, return the reference name (fallback)
    return refTypeName;
  }

  /**
   * Find concrete method in conCD by type name, method name, and parameter count.
   */
  private Optional<ASTCDMethod> findConcreteMethod(
      String typeName, String methodName, int paramCount) {

    // Find the concrete type
    return conIndex.methods(typeName, methodName).stream()
        .filter(conMethod -> conMethod.getCDParameterList().size() == paramCount)
        .findFirst();
  }

  /**
   * Find concrete type by name in conCD.
   */
  private Optional<ASTCDType> findConcreteType(String typeName) {
    return conIndex.type(typeName);
  }

  /***
   * build concrete name of an element form the matching found in the class diagram
   */
  protected String buildConcreteName(CodeMatching matching) {
    List<ISymbol> conReferences = new ArrayList<>();

    // resolve concrete references
    for (ISymbol refSymbol : matching.getReferences()) {
      // IncarnationContext
      Optional<ISymbol> fromContext = getSymbolFromContext(refSymbol);
      if (fromContext.isPresent()) {
        conReferences.add(fromContext.get());
        continue;
      }

      // Fall back to conformance checker
      if (refSymbol instanceof CDTypeSymbol) {
        conReferences.add(getConTypeSymbol((CDTypeSymbol) refSymbol));
      } else if (refSymbol instanceof FieldSymbol) {
        conReferences.add(getConAttributeSymbol((FieldSymbol) refSymbol));
      } else {
        // Handle method symbols and other symbol types
        conReferences.add(getConMethodSymbol(refSymbol));
      }
    }

    // Prefer an explicit generation template if provided
    String genTemplate = matching.getGenerateTemplate();
    if (genTemplate != null && !genTemplate.isEmpty()) {
      return MatcherHelper.fillTemplate(genTemplate, conReferences);
    }

    return MatcherHelper.fillTemplate(matching.getTemplate(), conReferences);
  }

  /**
   * Looks up a reference symbol's incarnation from the IncarnationContext.
   * Uses name-based matching since ISymbol objects from different loads may have different identities.
   */
  protected Optional<ISymbol> getSymbolFromContext(ISymbol refSymbol) {
    if (incarnationContext == null) {
      return Optional.empty();
    }

    List<ISymbol> incarnations = incarnationContext.getIncarnations(refSymbol);
    if (incarnations == null || incarnations.isEmpty()) {
      return Optional.empty();
    }
    if (refSymbol instanceof CDTypeSymbol && useCommonParentForMultipleIncarnations) {
      Optional<ISymbol> commonParent = findCommonParentInIncarnations(incarnations);
      if (commonParent.isPresent()) {
        return commonParent;
      }
      for (ISymbol incarnation : incarnations) {
        var grouping = incarnationContext.findGroupingTypeForImplementer(incarnation.getName());
        if (grouping.isPresent()) {
          Optional<ISymbol> groupingSymbol = findContextSymbolByName(grouping.get());
          if (groupingSymbol.isPresent()) {
            return groupingSymbol;
          }
        }
      }
      ISymbol first = incarnations.get(0);
      if (incarnationContext.getInterfaceToImplementers() != null) {
        for (Map.Entry<ISymbol, List<ISymbol>> e : incarnationContext.getInterfaceToImplementers().entrySet()) {
          List<ISymbol> impls = e.getValue();
          if (impls != null) {
            for (ISymbol impl : impls) {
              if (impl.getName().equals(first.getName())) {
                return Optional.of(e.getKey());
              }
            }
          }
        }
      }
    }
    return Optional.of(incarnations.get(0));
  }

  private Optional<ISymbol> findCommonParentInIncarnations(List<ISymbol> incarnations) {
    Set<String> incarnationNames = new HashSet<>();
    for (ISymbol incarnation : incarnations) {
      incarnationNames.add(incarnation.getName());
    }
    for (Map.Entry<ISymbol, List<ISymbol>> e : incarnationContext.getInterfaceToImplementers().entrySet()) {
      String parentName = e.getKey().getName();
      if (!incarnationNames.contains(parentName)) {
        continue;
      }
      Set<String> implementerNames = new HashSet<>();
      for (ISymbol implementer : e.getValue()) {
        implementerNames.add(implementer.getName());
      }
      Set<String> withoutParent = new HashSet<>(incarnationNames);
      withoutParent.remove(parentName);
      if (withoutParent.equals(implementerNames)) {
        return Optional.of(e.getKey());
      }
    }
    return Optional.empty();
  }

  private Optional<ISymbol> findContextSymbolByName(String name) {
    if (name == null || incarnationContext == null) {
      return Optional.empty();
    }
    for (Map.Entry<ISymbol, List<ISymbol>> e : incarnationContext.getInterfaceToImplementers().entrySet()) {
      if (e.getKey().getName().equals(name)) {
        return Optional.of(e.getKey());
      }
    }
    for (Map.Entry<ISymbol, List<ISymbol>> e : incarnationContext.getReferenceToIncarnations().entrySet()) {
      if (e.getKey().getName().equals(name)) {
        return Optional.of(e.getKey());
      }
      for (ISymbol incarnation : e.getValue()) {
        if (incarnation.getName().equals(name)) {
          return Optional.of(incarnation);
        }
      }
    }
    return Optional.empty();
  }

  private void registerConcreteMethodSignature(ISymbol referenceMethodSymbol, ISymbol concreteMethodSymbol) {
    if (concreteMethodSymbol == null || !(concreteMethodSymbol.getAstNode() instanceof ASTCDMethod)) {
      return;
    }
    if (incarnationContext != null) {
      Optional<StableElementKey> referenceKey = incarnationContext.getStableKey(referenceMethodSymbol);
      Optional<StableElementKey> concreteKey = incarnationContext.getStableKey(concreteMethodSymbol);
      if (referenceKey.isPresent() && concreteKey.isPresent()) {
        updater.registerMethodRewrite(referenceKey.get(), concreteKey.get());
      }
    }
    ASTCDMethod concreteMethod = (ASTCDMethod) concreteMethodSymbol.getAstNode();
    List<String> parameterTypes = new ArrayList<>();
    for (ASTCDParameter parameter : concreteMethod.getCDParameterList()) {
      parameterTypes.add(qualifyCdType(JavaSourceNames.printNormalizedType(parameter.getMCType())));
    }
    updater.registerConcreteMethodSignature(concreteMethod.getName(), parameterTypes);
  }

  private String qualifyCdType(String type) {
    return JavaSourceNames.replaceSimpleTypeNames(
        type,
        simpleName -> {
          if (conIndex.type(simpleName).isPresent() || refIndex.type(simpleName).isPresent()) {
            return Optional.empty();
          }
          return Optional.ofNullable(concreteImportedTypes.get(simpleName));
        });
  }

  private static Map<String, String> importedTypes(ASTCDCompilationUnit cd) {
    Map<String, String> imports = new LinkedHashMap<>();
    if (cd == null) {
      return imports;
    }
    for (var importStatement : cd.getMCImportStatementList()) {
      String imported = importStatement.getMCQualifiedName().getQName();
      // Wildcard imports do not provide an unambiguous simple-name mapping. java.lang types are
      // resolved implicitly by Java, so qualifying them only creates unnecessary generated imports.
      if (imported.endsWith(".*") || imported.startsWith("java.lang.")) {
        continue;
      }
      imports.putIfAbsent(JavaSourceNames.simpleName(imported), imported);
    }
    return imports;
  }

  protected ISymbol getConTypeSymbol(CDTypeSymbol symbol) {
    if (checker == null) {
      Log.warn("No CDConformanceChecker available for type " + symbol.getName());
      return symbol;
    }

    if (checker.getIncarnationMapping() == null) {
      Log.warn("No incarnation mapping available for type " + symbol.getName());
      return symbol;
    }

    var incarnations = checker.getIncarnationMapping().getIncarnations(symbol.getAstNode());
    if (incarnations != null && incarnations.iterator().hasNext()) {
      return incarnations.iterator().next().getSymbol();
    }

    for (ASTCDType refType : refIndex.types()) {
      if (refType.getName().equals(symbol.getName())) {
        incarnations = checker.getIncarnationMapping().getIncarnations(refType);
        if (incarnations != null && incarnations.iterator().hasNext()) {
          return incarnations.iterator().next().getSymbol();
        }
        break;
      }
    }

    Log.warn("No incarnation found for type " + symbol.getName() + "; using reference symbol");
    return symbol;
  }

  protected ISymbol getConAttributeSymbol(FieldSymbol symbol) {
    if (checker == null) {
      Log.warn("No CDConformanceChecker available for attribute " + symbol.getName());
      return symbol;
    }

    if (checker.getIncarnationMapping() == null) {
      Log.warn("No incarnation mapping available for attribute " + symbol.getName());
      return symbol;
    }

    var incarnations = checker.getIncarnationMapping().getIncarnations((ASTCDAttribute) symbol.getAstNode());
    if (incarnations != null && incarnations.iterator().hasNext()) {
      return incarnations.iterator().next().getSymbol();
    }

    Log.warn("No incarnation found for attribute " + symbol.getName() + "; using reference symbol");
    return symbol;
  }

  protected ISymbol getConMethodSymbol(ISymbol symbol) {
    if (checker == null) {
      Log.warn("No CDConformanceChecker available for method " + symbol.getName());
      return symbol;
    }

    if (symbol.getAstNode() instanceof ASTCDMethod && checker.getIncarnationMapping() != null) {
      var incarnations = checker.getIncarnationMapping().getIncarnations((ASTCDMethod) symbol.getAstNode());
      if (incarnations != null && incarnations.iterator().hasNext()) {
        return incarnations.iterator().next().getSymbol();
      }
    }

    Log.warn("No incarnation found for method " + symbol.getName() + "; using reference symbol");
    return symbol;
  }

  /**
   * Replace concrete implementer type names in a printed Java type string with the
   * grouping type (interface or class) mapped by the IncarnationContext. Handles simple
   * generic forms like List<T>, Map<K,V>, Optional<T> by recursive replacement of top-level args.
   */
  private String replaceConcreteWithGroupingType(String rawType) {
    if (rawType == null
        || rawType.isEmpty()
        || incarnationContext == null
        || !useCommonParentForMultipleIncarnations) return rawType;

    return JavaSourceNames.replaceSimpleTypeNames(
        rawType,
        simple -> {
          var grouping = incarnationContext.findGroupingTypeForImplementer(simple);
          if (grouping.isPresent()) {
            return grouping;
          }
          for (Map.Entry<ISymbol, List<ISymbol>> entry :
              incarnationContext.getReferenceToIncarnations().entrySet()) {
            if (entry.getKey().getName().equals(simple)) {
              List<ISymbol> incs = entry.getValue();
              if (incs != null && !incs.isEmpty()) {
                return incarnationContext.findGroupingTypeForImplementer(incs.get(0).getName());
              }
            }
          }
          return Optional.empty();
        });
  }
}
