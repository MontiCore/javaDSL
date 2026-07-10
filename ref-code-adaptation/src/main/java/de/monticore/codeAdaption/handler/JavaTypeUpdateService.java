package de.monticore.codeAdaption.handler;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.updater.CodeUpdater.MethodBodySpec;
import de.monticore.codeAdaption.utils.JavaLoader;
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
import java.util.List;
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
      removeTemplateMembers(templateType, templateFields, templateMethods);
      return;
    }

    String concreteName = resolveGeneratedTargetTypeName(templateType, typeMatching);
    List<GeneratedAttribute> attributes = collectGeneratedAttributes(concreteName);
    ASTFieldDeclaration fieldTemplate = templateFields.get(0);
    ASTMethodDeclaration methodTemplate = templateMethods.get(0);
    List<String> buildArguments = new ArrayList<>();
    for (GeneratedAttribute attribute : attributes) {
      String fieldName = attribute.name() + "Field";
      handler.updater.addField(templateType, fieldTemplate, fieldName, attribute.type());
      buildArguments.add(fieldName);
      handler.updater.addMethod(
          templateType,
          methodTemplate,
          setterName(attribute.name()),
          List.of(attribute.type()),
          List.of(attribute.name()),
          generatedTypeName,
          MethodBodySpec.assignFieldAndReturnThis(fieldName, attribute.name()));
    }
    handler.updater.addMethod(
        templateType,
        methodTemplate,
        "build",
        Collections.emptyList(),
        Collections.emptyList(),
        concreteName,
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
      String type = JavaLoader.print(attribute.getMCType());
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
                concreteMethod.getModifier().isStatic());
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
      List<ISymbol> incarnations = handler.incarnationContext.getIncarnations(reference);
      if (incarnations != null) {
        for (ISymbol incarnation : incarnations) {
          if (incarnation.getAstNode() instanceof ASTCDAttribute concreteAttribute) {
            result.add(concreteAttribute);
          }
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
      List<ISymbol> incarnations = handler.incarnationContext.getIncarnations(reference);
      if (incarnations != null) {
        for (ISymbol incarnation : incarnations) {
          if (incarnation.getAstNode() instanceof ASTCDMethod concreteMethod) {
            result.add(concreteMethod);
          }
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
        handler.incarnationContext.getStableKey(selected.get());
    Optional<de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey> concreteKey =
        handler.incarnationContext.getStableKey(concrete);
    return selectedKey.isPresent() && selectedKey.equals(concreteKey);
  }

  private static String setterName(String attributeName) {
    return attributeName == null || attributeName.isEmpty()
        ? "set"
        : "set" + JavaSourceNames.capitalize(attributeName);
  }

  private record GeneratedAttribute(String name, String type) {}
}
