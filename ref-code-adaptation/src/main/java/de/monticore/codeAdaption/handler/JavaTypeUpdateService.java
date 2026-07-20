package de.monticore.codeAdaption.handler;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.cdinterfaceandenum._ast.ASTCDEnum;
import de.monticore.cdinterfaceandenum._ast.ASTCDInterface;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.CDTypeRelations;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Adapts and generates Java types and projects members introduced by CD completion. */
final class JavaTypeUpdateService {
  private final BasicUpdateHandler handler;
  private final ConcreteSymbolResolver symbols;
  private final JavaMemberUpdateService memberUpdates;
  private final Set<String> generatedTypes = new HashSet<>();

  JavaTypeUpdateService(
      BasicUpdateHandler handler,
      ConcreteSymbolResolver symbols,
      JavaMemberUpdateService memberUpdates) {
    this.handler = handler;
    this.symbols = symbols;
    this.memberUpdates = memberUpdates;
  }

  void beginRun() {
    generatedTypes.clear();
  }

  void handleTypeUpdate(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      Optional<CodeMatching> matching = handler.validator.getMatchedType(type);
      if (matching.isEmpty() || !matching.get().mustBePerform()) {
        continue;
      }
      if (matching.get().getGenerateTemplate() != null
          && !matching.get().getGenerateTemplate().isEmpty()) {
        generateTypeFromTemplate(type, matching.get(), collector);
      } else {
        handler.updater.updateType(type, handler.buildConcreteName(matching.get()));
      }
    }

    for (ASTCDType cdType : handler.refIndex.types()) {
      String concreteName = symbols.resolveConcreteTypeName(cdType.getName());
      handler.updater.updateCDType(cdType, concreteName);
    }
  }

  private void generateTypeFromTemplate(
      ASTTypeDeclaration templateType,
      CodeMatching typeMatching,
      JavaAstElemCollector collector) {
    String generatedTypeName = handler.buildConcreteName(typeMatching);
    if (!generatedTypes.add(generatedTypeName)) {
      return;
    }
    handler.updater.addType(templateType, generatedTypeName);

    List<ASTFieldDeclaration> templateFields = collector.getAllFieldDeclarations(templateType);
    List<ASTMethodDeclaration> templateMethods = collector.getAllMethodDeclarations(templateType);
    if (templateFields.isEmpty() || templateMethods.isEmpty()) {
      // A plain type template (for example Observer -> Attacker) is still a complete source
      // template. Builder expansion needs both a field and a method prototype, but the absence of
      // either prototype must not erase the declarations that the renamed type already owns.
      return;
    }

    String concreteName = resolveGeneratedTargetTypeName(templateType, typeMatching);
    List<GeneratedAttribute> attributes = collectGeneratedAttributes(concreteName);
    ASTFieldDeclaration fieldTemplate = templateFields.get(0);
    ASTMethodDeclaration methodTemplate = templateMethods.get(0);
    List<String> buildArguments = new ArrayList<>();
    for (GeneratedAttribute attribute : attributes) {
      String fieldName = attribute.name() + "Field";
      handler.updater.addField(templateType, fieldTemplate, fieldName, attribute.type(), false);
      buildArguments.add(fieldName);
      handler.updater.addMethod(
          templateType,
          methodTemplate,
          setterName(attribute.name()),
          List.of(attribute.type()),
          List.of(attribute.name()),
          generatedTypeName,
          false,
          MethodBodySpec.assignFieldAndReturnThis(fieldName, attribute.name()));
    }
    handler.updater.addMethod(
        templateType,
        methodTemplate,
        "build",
        Collections.emptyList(),
        Collections.emptyList(),
        concreteName,
        false,
        MethodBodySpec.returnNew(concreteName, buildArguments));
    removeTemplateMembers(templateType, templateFields, templateMethods);
  }

  private String resolveGeneratedTargetTypeName(
      ASTTypeDeclaration templateType, CodeMatching matching) {
    for (ISymbol reference : matching.getReferences()) {
      if (reference instanceof CDTypeSymbol type) {
        return handler
            .getSymbolFromContext(reference)
            .orElseGet(() -> handler.getConTypeSymbol(type))
            .getName();
      }
    }
    return symbols.resolveConcreteTypeName(templateType.getName());
  }

  private List<GeneratedAttribute> collectGeneratedAttributes(String concreteName) {
    Optional<ASTCDType> concreteType = symbols.findConcreteType(concreteName);
    if (concreteType.isEmpty()) {
      Log.warn("Cannot generate members: concrete type " + concreteName + " is not in the CD");
      return List.of();
    }
    List<GeneratedAttribute> attributes = new ArrayList<>();
    for (ASTCDAttribute attribute : concreteType.get().getCDAttributeList()) {
      String type = JavaSourceNames.printNormalizedType(attribute.getMCType());
      if (handler.incarnationContext != null
          && handler.useCommonParentForMultipleIncarnations) {
        type = symbols.replaceConcreteWithGroupingType(type);
      }
      attributes.add(
          new GeneratedAttribute(attribute.getName(), symbols.resolveConcreteCdType(type)));
    }
    return attributes;
  }

  private void removeTemplateMembers(
      ASTTypeDeclaration templateType,
      List<ASTFieldDeclaration> templateFields,
      List<ASTMethodDeclaration> templateMethods) {
    for (ASTFieldDeclaration field : templateFields) {
      try {
        handler.updater.removeField(templateType, field);
      } catch (RuntimeException exception) {
        Log.debug(
            "Could not remove template field: " + exception.getMessage(),
            "JavaTypeUpdateService");
      }
    }
    for (ASTMethodDeclaration method : templateMethods) {
      try {
        handler.updater.removeMethod(templateType, method);
      } catch (RuntimeException exception) {
        Log.debug(
            "Could not remove template method: " + exception.getMessage(),
            "JavaTypeUpdateService");
      }
    }
  }

  void projectCompletedMembers(JavaAstElemCollector collector) {
    for (ASTTypeDeclaration type : collector.getAllTypeDeclarations()) {
      Optional<CodeMatching> typeMatching = handler.validator.getMatchedType(type);
      if (typeMatching.isEmpty() || !typeMatching.get().mustBePerform()) {
        continue;
      }
      String concreteTypeName = handler.buildConcreteName(typeMatching.get());
      Optional<ASTCDType> concreteType = handler.conIndex.type(concreteTypeName);
      if (concreteType.isEmpty()) {
        continue;
      }
      projectFields(type, concreteType.get(), collector);
      projectMethods(type, concreteType.get(), collector);
      projectCompletionDelta(type, concreteType.get());
    }
  }

  private void projectCompletionDelta(ASTTypeDeclaration javaType, ASTCDType completedType) {
    Optional<ASTCDType> inputType = handler.inputConIndex.type(completedType.getName());

    boolean completedAbstract = CDTypeRelations.isAbstract(completedType);
    if (completedType instanceof ASTCDClass) {
      // Reference templates may be abstract even when a concrete incarnation is not. Normalize
      // every adapted class to the final target CD instead of treating abstractness only as a
      // completion delta.
      handler.updater.setTypeAbstract(javaType, completedAbstract);
    }

    for (ASTCDAttribute completedField : completedType.getCDAttributeList()) {
      Optional<ASTCDAttribute> inputField =
          inputType.flatMap(
              type -> handler.inputConIndex.attribute(type.getName(), completedField.getName()));
      String completedFieldType = JavaSourceNames.printNormalizedFieldType(completedField);
      boolean unchanged =
          inputField
              .map(JavaSourceNames::printNormalizedFieldType)
              .filter(completedFieldType::equals)
              .isPresent();
      if (!unchanged) {
        handler.updater.addField(
            javaType,
            null,
            completedField.getName(),
            symbols.resolveConcreteCdType(completedFieldType),
            completedField.getModifier().isStatic());
      }
    }

    for (ASTCDMethod completedMethod : completedType.getCDMethodList()) {
      String signature = JavaSourceNames.methodSignature(completedMethod);
      Optional<ASTCDMethod> inputMethod =
          inputType.flatMap(type -> handler.inputConIndex.method(type.getName(), signature));
      String completedReturnType = JavaSourceNames.printNormalizedReturnType(completedMethod);
      boolean unchanged =
          inputMethod
              .map(JavaSourceNames::printNormalizedReturnType)
              .filter(completedReturnType::equals)
              .isPresent();
      if (!unchanged) {
        handler.updater.addMethod(
            javaType,
            null,
            completedMethod.getName(),
            completedMethod.getCDParameterList().stream()
                .map(ASTCDParameter::getMCType)
                .map(JavaSourceNames::printNormalizedType)
                .map(symbols::resolveConcreteCdType)
                .toList(),
            completedMethod.getCDParameterList().stream().map(ASTCDParameter::getName).toList(),
            symbols.resolveConcreteCdType(completedReturnType),
            completedMethod.getModifier().isStatic(),
            MethodBodySpec.empty());
      }
    }

    String inputSuperclass =
        inputType.flatMap(CDTypeRelations::firstSuperclassName)
            .map(JavaSourceNames::simpleName)
            .orElse(null);
    String completedSuperclass =
        CDTypeRelations.firstSuperclassName(completedType)
            .map(JavaSourceNames::simpleName)
            .orElse(null);
    if (completedSuperclass != null && !completedSuperclass.equals(inputSuperclass)) {
      handler.updater.addSuperType(javaType, completedSuperclass, false);
    }

    Set<String> inputInterfaces =
        inputType.stream()
            .flatMap(type -> CDTypeRelations.interfaceNames(type).stream())
            .map(JavaSourceNames::simpleName)
            .collect(java.util.stream.Collectors.toSet());
    List<String> addedInterfaces = CDTypeRelations.interfaceNames(completedType).stream()
        .map(JavaSourceNames::simpleName)
        .filter(interfaceName -> !inputInterfaces.contains(interfaceName))
        .distinct()
        .toList();
    addedInterfaces.forEach(
        interfaceName -> handler.updater.addSuperType(javaType, interfaceName, true));
    if (completedType instanceof ASTCDClass && !completedAbstract) {
      Set<String> completedEffectiveInterfaces =
          effectiveInterfaceNames(handler.conIndex, completedType.getName());
      Set<String> inputEffectiveInterfaces =
          inputType
              .map(type -> effectiveInterfaceNames(handler.inputConIndex, type.getName()))
              .orElseGet(Set::of);
      List<String> introducedEffectiveInterfaces =
          completedEffectiveInterfaces.stream()
              .filter(interfaceName -> !inputEffectiveInterfaces.contains(interfaceName))
              .toList();
      projectInterfaceContracts(javaType, completedType, introducedEffectiveInterfaces);
    }

    if (completedType instanceof ASTCDEnum completedEnum) {
      Set<String> inputConstants =
          inputType
              .filter(ASTCDEnum.class::isInstance)
              .map(ASTCDEnum.class::cast)
              .stream()
              .flatMap(type -> type.getCDEnumConstantList().stream())
              .map(constant -> constant.getName())
              .collect(java.util.stream.Collectors.toSet());
      for (int index = 0; index < completedEnum.getCDEnumConstantList().size(); index++) {
        String name = completedEnum.getCDEnumConstant(index).getName();
        if (!inputConstants.contains(name)) {
          handler.updater.addEnumConstant(javaType, name, index);
        }
      }
    }
  }

  /**
   * Returns every interface implemented directly or inherited through interfaces and
   * superclasses. The visited set in the recursive helper makes malformed inheritance cycles safe.
   */
  static Set<String> effectiveInterfaceNames(CDModelIndex index, String typeName) {
    Set<String> result = new LinkedHashSet<>();
    collectEffectiveInterfaces(index, typeName, result, new LinkedHashSet<>());
    return result;
  }

  private static void collectEffectiveInterfaces(
      CDModelIndex index,
      String typeName,
      Set<String> interfaces,
      Set<String> visitedTypes) {
    String simpleName = JavaSourceNames.simpleName(typeName);
    if (!visitedTypes.add(simpleName)) {
      return;
    }
    ASTCDType type = index.type(simpleName).orElse(null);
    if (type == null) {
      return;
    }
    for (String interfaceName : CDTypeRelations.interfaceNames(type)) {
      String simpleInterface = JavaSourceNames.simpleName(interfaceName);
      interfaces.add(simpleInterface);
      collectEffectiveInterfaces(index, simpleInterface, interfaces, visitedTypes);
    }
    CDTypeRelations.firstSuperclassName(type)
        .ifPresent(parent -> collectEffectiveInterfaces(index, parent, interfaces, visitedTypes));
  }

  /**
   * Materializes newly introduced interface contracts on a concrete Java class. Abstract classes
   * intentionally skip this step; their concrete descendants receive any still-missing contracts.
   */
  private void projectInterfaceContracts(
      ASTTypeDeclaration javaType, ASTCDType completedType, List<String> addedInterfaces) {
    Map<String, ASTCDMethod> contracts = new LinkedHashMap<>();
    Set<String> visitedInterfaces = new LinkedHashSet<>();
    for (String interfaceName : addedInterfaces) {
      collectInterfaceContracts(interfaceName, contracts, visitedInterfaces);
    }

    for (ASTCDMethod contract : contracts.values()) {
      validateCompletedImplementation(completedType, contract);
      handler.updater.addMethod(
          javaType,
          null,
          contract.getName(),
          contract.getCDParameterList().stream()
              .map(ASTCDParameter::getMCType)
              .map(JavaSourceNames::printNormalizedType)
              .map(symbols::resolveConcreteCdType)
              .toList(),
          contract.getCDParameterList().stream().map(ASTCDParameter::getName).toList(),
          symbols.resolveConcreteCdType(JavaSourceNames.printNormalizedReturnType(contract)),
          false,
          MethodBodySpec.interfaceContract());
    }
  }

  /**
   * Traverses parent interfaces first and keeps one method per normalized signature. Compatible
   * covariant returns select the most specific declaration; incompatible returns fail explicitly.
   */
  private void collectInterfaceContracts(
      String interfaceName,
      Map<String, ASTCDMethod> contracts,
      Set<String> visitedInterfaces) {
    String simpleName = JavaSourceNames.simpleName(interfaceName);
    if (!visitedInterfaces.add(simpleName)) {
      return;
    }
    ASTCDType interfaceType = handler.conIndex.type(simpleName).orElse(null);
    if (!(interfaceType instanceof ASTCDInterface)) {
      throw new IllegalStateException(
          "Completed Java interface '" + simpleName + "' is not an interface in the concrete CD");
    }
    for (String parent : CDTypeRelations.interfaceNames(interfaceType)) {
      collectInterfaceContracts(parent, contracts, visitedInterfaces);
    }
    for (ASTCDMethod method : interfaceType.getCDMethodList()) {
      if (method.getModifier().isStatic()) {
        continue;
      }
      String signature = JavaSourceNames.methodSignature(method);
      ASTCDMethod existing = contracts.get(signature);
      if (existing != null) {
        String existingReturn = JavaSourceNames.printNormalizedReturnType(existing);
        String candidateReturn = JavaSourceNames.printNormalizedReturnType(method);
        if (!returnsAreCompatible(existingReturn, candidateReturn)) {
          throw new IllegalStateException(
              "Incompatible inherited interface contracts for "
                  + signature
                  + ": "
                  + existingReturn
                  + " and "
                  + candidateReturn);
        }
        if (isCovariantReturn(candidateReturn, existingReturn)) {
          contracts.put(signature, method);
        }
      } else {
        contracts.put(signature, method);
      }
    }
  }

  private boolean returnsAreCompatible(String first, String second) {
    return isCompatibleImplementationReturn(handler.conIndex, first, second)
        || isCompatibleImplementationReturn(handler.conIndex, second, first);
  }

  private boolean isCovariantReturn(String candidate, String parent) {
    return !JavaSourceNames.normalizeType(candidate)
            .equals(JavaSourceNames.normalizeType(parent))
        && isCompatibleImplementationReturn(handler.conIndex, candidate, parent);
  }

  /** Tests Java-compatible equality or covariance for an implementation return type. */
  static boolean isCompatibleImplementationReturn(
      CDModelIndex index, String actualReturn, String expectedReturn) {
    String actual = JavaSourceNames.normalizeType(actualReturn);
    String expected = JavaSourceNames.normalizeType(expectedReturn);
    if (actual.equals(expected)) {
      return true;
    }
    if ("Object".equals(JavaSourceNames.simpleName(expected)) && !isPrimitive(actual)) {
      return true;
    }
    return index.isSubtypeOf(actual, expected);
  }

  private static boolean isPrimitive(String type) {
    return Set.of("boolean", "byte", "short", "int", "long", "char", "float", "double", "void")
        .contains(type);
  }

  /** Rejects a completed class method that already occupies a contract signature incompatibly. */
  private void validateCompletedImplementation(ASTCDType completedType, ASTCDMethod contract) {
    String signature = JavaSourceNames.methodSignature(contract);
    for (ASTCDMethod method : completedType.getCDMethodList()) {
      if (!signature.equals(JavaSourceNames.methodSignature(method))) {
        continue;
      }
      String expectedReturn = JavaSourceNames.printNormalizedReturnType(contract);
      String actualReturn = JavaSourceNames.printNormalizedReturnType(method);
      if (!isCompatibleImplementationReturn(
          handler.conIndex, actualReturn, expectedReturn)) {
        throw new IllegalStateException(
            "Method "
                + completedType.getName()
                + "."
                + signature
                + " has return type "
                + actualReturn
                + " but interface contract requires "
                + expectedReturn);
      }
      return;
    }
  }

  private void projectFields(
      ASTTypeDeclaration type, ASTCDType concreteType, JavaAstElemCollector collector) {
    for (ASTFieldDeclaration template : collector.getAllFieldDeclarations(type)) {
      Optional<CodeMatching> matching = handler.validator.getMatchedField(type, template);
      if (matching.isEmpty() || !matching.get().mustBePerform()) {
        continue;
      }
      boolean generated = false;
      boolean retainsTemplate = false;
      for (ISymbol reference : matching.get().getReferences()) {
        if (!(reference.getAstNode() instanceof ASTCDAttribute)) {
          continue;
        }
        for (ASTCDAttribute concreteAttribute : concreteAttributesFor(reference)) {
          if (isOwnedBy(concreteAttribute, concreteType)) {
            generated = true;
            if (isSelectedIncarnation(reference, concreteAttribute.getSymbol())) {
              retainsTemplate = true;
              continue;
            }
            retainsTemplate |= matches(template, concreteAttribute);
            handler.updater.addField(
                type,
                template,
                concreteAttribute.getName(),
                symbols.resolveConcreteCdType(
                    JavaSourceNames.printNormalizedFieldType(concreteAttribute)),
                concreteAttribute.getModifier().isStatic());
          }
        }
      }
      if (generated && !retainsTemplate) {
        handler.updater.removeField(type, template);
      }
    }
  }

  private void projectMethods(
      ASTTypeDeclaration type, ASTCDType concreteType, JavaAstElemCollector collector) {
    for (ASTMethodDeclaration template : collector.getAllMethodDeclarations(type)) {
      Optional<CodeMatching> matching = handler.validator.getMatchedMethod(type, template);
      if (matching.isEmpty() || !matching.get().mustBePerform()) {
        continue;
      }
      boolean generated = false;
      boolean retainsTemplate = false;
      for (ISymbol reference : matching.get().getReferences()) {
        if (!(reference.getAstNode() instanceof ASTCDMethod)
            || memberUpdates.isForEachTargetMethod(reference)) {
          continue;
        }
        for (ASTCDMethod concreteMethod : concreteMethodsFor(reference)) {
          if (isOwnedBy(concreteMethod, concreteType)) {
            generated = true;
            retainsTemplate |= matches(template, concreteMethod, type, collector);
            handler.updater.addMethod(
                type,
                template,
                concreteMethod.getName(),
                concreteMethod.getCDParameterList().stream()
                    .map(
                        parameter ->
                            symbols.resolveConcreteCdType(
                                JavaSourceNames.printNormalizedType(parameter.getMCType())))
                    .toList(),
                concreteMethod.getCDParameterList().stream()
                    .map(ASTCDParameter::getName)
                    .toList(),
                symbols.resolveConcreteCdType(
                    JavaSourceNames.printNormalizedReturnType(concreteMethod)),
                concreteMethod.getModifier().isStatic(),
                MethodBodySpec.empty());
          }
        }
      }
      if (generated && !retainsTemplate) {
        handler.updater.removeMethod(type, template);
      }
    }
  }

  private boolean matches(ASTFieldDeclaration template, ASTCDAttribute concrete) {
    String templateName = template.getVariableDeclarator(0).getDeclarator().getName();
    return templateName.equals(concrete.getName())
        && JavaSourceNames.printNormalizedType(template.getMCType())
            .equals(JavaSourceNames.printNormalizedFieldType(concrete));
  }

  private boolean matches(
      ASTMethodDeclaration template,
      ASTCDMethod concrete,
      ASTTypeDeclaration owner,
      JavaAstElemCollector collector) {
    List<String> templateParameters =
        collector.getAllParameters(owner, template).stream()
            .map(ASTFormalParameter::getMCType)
            .map(JavaSourceNames::printNormalizedType)
            .toList();
    List<String> concreteParameters =
        concrete.getCDParameterList().stream()
            .map(ASTCDParameter::getMCType)
            .map(JavaSourceNames::printNormalizedType)
            .toList();
    return template.getName().equals(concrete.getName())
        && templateParameters.equals(concreteParameters)
        && JavaSourceNames.normalizeType(JavaLoader.print(template.getMCReturnType()))
            .equals(JavaSourceNames.printNormalizedReturnType(concrete));
  }

  private List<ASTCDAttribute> concreteAttributesFor(ISymbol reference) {
    List<ASTCDAttribute> result = new ArrayList<>();
    if (reference.getAstNode() instanceof ASTCDAttribute attribute
        && handler.checker != null
        && handler.checker.getIncarnationMapping() != null) {
      var incarnations = handler.checker.getIncarnationMapping().getIncarnations(attribute);
      if (incarnations != null) {
        for (var incarnation : incarnations) {
          if (incarnation instanceof ASTCDAttribute concreteAttribute) {
            result.add(concreteAttribute);
          }
        }
      }
    }
    if (result.isEmpty() && handler.incarnationContext != null) {
      for (var incarnation : handler.mappedIncarnations(reference)) {
        if (incarnation.symbol().getAstNode() instanceof ASTCDAttribute concreteAttribute) {
          result.add(concreteAttribute);
        }
      }
    }
    return result;
  }

  private List<ASTCDMethod> concreteMethodsFor(ISymbol reference) {
    List<ASTCDMethod> result = new ArrayList<>();
    if (reference.getAstNode() instanceof ASTCDMethod method
        && handler.checker != null
        && handler.checker.getIncarnationMapping() != null) {
      var incarnations = handler.checker.getIncarnationMapping().getIncarnations(method);
      if (incarnations != null) {
        for (var incarnation : incarnations) {
          if (incarnation instanceof ASTCDMethod concreteMethod) {
            result.add(concreteMethod);
          }
        }
      }
    }
    if (result.isEmpty() && handler.incarnationContext != null) {
      for (var incarnation : handler.mappedIncarnations(reference)) {
        if (incarnation.symbol().getAstNode() instanceof ASTCDMethod concreteMethod) {
          result.add(concreteMethod);
        }
      }
    }
    return result;
  }

  private boolean isOwnedBy(ASTCDAttribute attribute, ASTCDType owner) {
    return handler.conIndex.ownerOf(attribute)
        .map(candidate -> candidate == owner || candidate.getName().equals(owner.getName()))
        .orElse(false);
  }

  private boolean isOwnedBy(ASTCDMethod method, ASTCDType owner) {
    return handler.conIndex.ownerOf(method)
        .map(candidate -> candidate == owner || candidate.getName().equals(owner.getName()))
        .orElse(false);
  }

  private boolean isSelectedIncarnation(ISymbol reference, ISymbol concrete) {
    Optional<ISymbol> selected = handler.getSymbolFromContext(reference);
    if (selected.isEmpty()) {
      return false;
    }
    if (selected.get() == concrete || selected.get().getAstNode() == concrete.getAstNode()) {
      return true;
    }
    if (handler.incarnationContext == null) {
      return false;
    }
    Optional<de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey> selectedKey =
        handler.concreteKey(selected.get());
    Optional<de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey> concreteKey =
        handler.concreteKey(concrete);
    return selectedKey.isPresent() && selectedKey.equals(concreteKey);
  }

  private static String setterName(String attributeName) {
    return attributeName == null || attributeName.isEmpty()
        ? "set"
        : "set" + JavaSourceNames.capitalize(attributeName);
  }

  private record GeneratedAttribute(String name, String type) {}
}
