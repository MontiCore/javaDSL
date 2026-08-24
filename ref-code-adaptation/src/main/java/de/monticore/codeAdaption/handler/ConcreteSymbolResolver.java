package de.monticore.codeAdaption.handler;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Resolves reference-CD symbols, signatures, and Java type names to concrete counterparts. */
final class ConcreteSymbolResolver {
  private final BasicUpdateHandler handler;
  private final Map<String, Set<String>> concreteImportedTypes;
  private final Set<String> wildcardImportPackages;

  ConcreteSymbolResolver(
      BasicUpdateHandler handler,
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD) {
    this.handler = handler;
    this.concreteImportedTypes = importedTypes(concreteCD, referenceCD);
    this.wildcardImportPackages = wildcardImportPackages(concreteCD, referenceCD);
  }

  String buildConcreteName(CodeMatching matching) {
    List<ISymbol> concreteReferences = new ArrayList<>();
    for (ISymbol reference : matching.getReferences()) {
      Optional<ISymbol> fromContext = handler.getSymbolFromContext(reference);
      if (fromContext.isPresent()) {
        concreteReferences.add(fromContext.get());
      } else if (reference instanceof CDTypeSymbol type) {
        concreteReferences.add(handler.getConTypeSymbol(type));
      } else if (reference instanceof FieldSymbol field) {
        concreteReferences.add(handler.getConAttributeSymbol(field));
      } else {
        concreteReferences.add(handler.getConMethodSymbol(reference));
      }
    }
    String generationTemplate = matching.getGenerateTemplate();
    return MatcherHelper.fillTemplate(
        generationTemplate != null && !generationTemplate.isEmpty()
            ? generationTemplate
            : matching.getTemplate(),
        concreteReferences);
  }

  Optional<ISymbol> getSymbolFromContext(ISymbol reference) {
    if (handler.incarnationContext == null) {
      return Optional.empty();
    }
    Optional<IncarnationContext.MappedElement> selected = selectedIncarnation(reference);
    if (selected.isPresent()) {
      return selected.map(IncarnationContext.MappedElement::symbol);
    }
    List<IncarnationContext.MappedElement> mappedIncarnations =
        handler.mappedIncarnations(reference);
    if (mappedIncarnations.isEmpty()) {
      return Optional.empty();
    }
    List<ISymbol> incarnations =
        mappedIncarnations.stream().map(IncarnationContext.MappedElement::symbol).toList();
    if (reference instanceof CDTypeSymbol && handler.useCommonParentForMultipleIncarnations) {
      Optional<ISymbol> commonParent = findCommonParent(incarnations);
      if (commonParent.isPresent()) {
        return commonParent;
      }
      for (ISymbol incarnation : incarnations) {
        Optional<String> grouping = groupingName(incarnation.getName());
        if (grouping.isPresent()) {
          Optional<ISymbol> groupingSymbol = findContextSymbolByName(grouping.get());
          if (groupingSymbol.isPresent()) {
            return groupingSymbol;
          }
        }
      }
    }
    return Optional.of(incarnations.get(0));
  }

  private Optional<IncarnationContext.MappedElement> selectedIncarnation(ISymbol reference) {
    Optional<StableElementKey> referenceKey = handler.referenceKey(reference);
    if (referenceKey.isEmpty()) {
      return Optional.empty();
    }
    IncarnationContext.MappedElement direct =
        handler.incarnationSelection.get(referenceKey.get());
    if (direct != null) {
      return Optional.of(direct);
    }
    Optional<String> referenceOwner = referenceKey.get().getOwnerType();
    if (referenceOwner.isEmpty()) {
      return Optional.empty();
    }
    IncarnationContext.MappedElement selectedOwner =
        handler.incarnationSelection.get(StableElementKey.type(referenceOwner.get()));
    if (selectedOwner == null) {
      return Optional.empty();
    }
    return handler.mappedIncarnations(reference).stream()
        .filter(
            incarnation ->
                incarnation
                    .key()
                    .getOwnerType()
                    .filter(selectedOwner.key().getName()::equals)
                    .isPresent())
        .findFirst();
  }

  ISymbol getConTypeSymbol(CDTypeSymbol symbol) {
    if (handler.checker == null || handler.checker.getIncarnationMapping() == null) {
      Log.warn("No conformance incarnation mapping available for type " + symbol.getName());
      return symbol;
    }
    var incarnations = handler.checker.getIncarnationMapping().getIncarnations(symbol.getAstNode());
    if (incarnations != null && incarnations.iterator().hasNext()) {
      return incarnations.iterator().next().getSymbol();
    }
    for (ASTCDType referenceType : handler.refIndex.types()) {
      if (referenceType.getName().equals(symbol.getName())) {
        incarnations = handler.checker.getIncarnationMapping().getIncarnations(referenceType);
        if (incarnations != null && incarnations.iterator().hasNext()) {
          return incarnations.iterator().next().getSymbol();
        }
        break;
      }
    }
    Log.warn("No incarnation found for type " + symbol.getName() + "; using reference symbol");
    return symbol;
  }

  ISymbol getConAttributeSymbol(FieldSymbol symbol) {
    if (handler.checker == null || handler.checker.getIncarnationMapping() == null) {
      Log.warn("No conformance incarnation mapping available for attribute " + symbol.getName());
      return symbol;
    }
    if (!(symbol.getAstNode() instanceof ASTCDAttribute attribute)) {
      throw new IllegalStateException(
          "Field symbol " + symbol.getName() + " has no ASTCDAttribute node");
    }
    var incarnations = handler.checker.getIncarnationMapping().getIncarnations(attribute);
    if (incarnations != null && incarnations.iterator().hasNext()) {
      return incarnations.iterator().next().getSymbol();
    }
    Log.warn("No incarnation found for attribute " + symbol.getName() + "; using reference symbol");
    return symbol;
  }

  ISymbol getConMethodSymbol(ISymbol symbol) {
    if (handler.checker != null
        && handler.checker.getIncarnationMapping() != null
        && symbol.getAstNode() instanceof ASTCDMethod method) {
      var incarnations = handler.checker.getIncarnationMapping().getIncarnations(method);
      if (incarnations != null && incarnations.iterator().hasNext()) {
        return incarnations.iterator().next().getSymbol();
      }
    }
    Log.warn("No incarnation found for method " + symbol.getName() + "; using reference symbol");
    return symbol;
  }

  Optional<ISymbol> resolveConcreteMethodSymbol(CodeMatching matching) {
    for (ISymbol reference : matching.getReferences()) {
      if (!(reference.getAstNode() instanceof ASTCDMethod)) {
        continue;
      }
      ISymbol concrete =
          handler.getSymbolFromContext(reference).orElseGet(() -> handler.getConMethodSymbol(reference));
      if (concrete != reference && concrete.getAstNode() instanceof ASTCDMethod) {
        return Optional.of(concrete);
      }
    }
    return Optional.empty();
  }

  void registerConcreteMethodSignature(
      de.monticore.java.javadsl._ast.ASTTypeDeclaration sourceOwner,
      ISymbol reference,
      ISymbol concrete) {
    if (concrete == null || !(concrete.getAstNode() instanceof ASTCDMethod concreteMethod)) {
      return;
    }
    if (handler.incarnationContext != null) {
      Optional<StableElementKey> referenceKey = handler.referenceKey(reference);
      Optional<StableElementKey> concreteKey = handler.concreteKey(concrete);
      if (referenceKey.isPresent() && concreteKey.isPresent()) {
        List<String> qualifiedParameterTypes =
            concreteMethod.getCDParameterList().stream()
                .map(
                    parameter ->
                        qualifyCdType(JavaSourceNames.printNormalizedType(parameter.getMCType())))
                .toList();
        StableElementKey qualifiedConcreteKey =
            StableElementKey.method(
                concreteKey.get().getOwnerType().orElse(null),
                concreteKey.get().getName(),
                qualifiedParameterTypes,
                concreteKey.get().getReturnType().orElse(null));
        handler.updater.registerMethodRewrite(
            sourceOwner, referenceKey.get(), qualifiedConcreteKey);
      }
    }
    handler.updater.registerConcreteMethodSignature(
        concreteMethod.getName(),
        concreteMethod.getCDParameterList().stream()
            .map(parameter -> qualifyCdType(JavaSourceNames.printNormalizedType(parameter.getMCType())))
            .toList());
  }

  Optional<ASTCDMethod> findConcreteMethod(
      String concreteOwner,
      String concreteMethodName,
      List<ASTFormalParameter> referenceParameters) {
    List<ASTCDMethod> candidates =
        handler.conIndex.methods(concreteOwner, concreteMethodName).stream()
            .filter(method -> method.getCDParameterList().size() == referenceParameters.size())
            .toList();
    if (candidates.size() <= 1) {
      return candidates.stream().findFirst();
    }
    List<String> expectedTypes =
        referenceParameters.stream()
            .map(parameter -> concreteType(JavaSourceNames.printNormalizedType(parameter.getMCType())))
            .toList();
    List<ASTCDMethod> exact =
        candidates.stream()
            .filter(method -> normalizedParameterTypes(method).equals(expectedTypes))
            .toList();
    if (exact.size() == 1) {
      return Optional.of(exact.get(0));
    }
    Log.warn(
        "Ambiguous concrete overload "
            + concreteOwner
            + "."
            + concreteMethodName
            + "("
            + String.join(",", expectedTypes)
            + ")");
    return Optional.empty();
  }

  String resolveConcreteTypeName(String referenceTypeName) {
    Optional<ASTCDType> referenceType = handler.refIndex.type(JavaSourceNames.simpleName(referenceTypeName));
    if (referenceType.isPresent()) {
      ISymbol resolved =
          handler
              .getSymbolFromContext(referenceType.get().getSymbol())
              .orElseGet(() -> handler.getConTypeSymbol(referenceType.get().getSymbol()));
      if (resolved.getName().equals(referenceType.get().getName())
          && handler.incarnationContext != null) {
        Optional<String> concreteAlternative =
            handler.mappedIncarnations(referenceType.get().getSymbol()).stream()
                .map(element -> element.key().getName())
                .filter(name -> !name.equals(referenceType.get().getName()))
                .filter(handler.conIndex::hasType)
                .sorted()
                .findFirst();
        if (concreteAlternative.isPresent()) {
          return concreteAlternative.get();
        }
      }
      return resolved.getName();
    }
    return handler.conIndex.type(JavaSourceNames.simpleName(referenceTypeName)).isPresent()
        ? JavaSourceNames.simpleName(referenceTypeName)
        : referenceTypeName;
  }

  Optional<ASTCDType> findConcreteType(String typeName) {
    return handler.conIndex.type(JavaSourceNames.simpleName(typeName));
  }

  String qualifyCdType(String type) {
    return JavaSourceNames.replaceTypeNames(
        type,
        reference -> {
          if (reference.qualified()) {
            return Optional.empty();
          }
          String simpleName = reference.simpleName();
          if (handler.conIndex.type(simpleName).isPresent()
              || handler.refIndex.type(simpleName).isPresent()) {
            return Optional.empty();
          }
          Set<String> candidates = concreteImportedTypes.get(simpleName);
          if (candidates == null || candidates.isEmpty()) {
            LinkedHashSet<String> classpathCandidates = new LinkedHashSet<>();
            String javaLang = "java.lang." + simpleName;
            if (classpathTypeExists(javaLang)) {
              classpathCandidates.add(javaLang);
            }
            for (String packageName : wildcardImportPackages) {
              String candidate = packageName + "." + simpleName;
              if (classpathTypeExists(candidate)) {
                classpathCandidates.add(candidate);
              }
            }
            candidates = classpathCandidates;
          }
          if (candidates.size() == 1) {
            return Optional.of(candidates.iterator().next());
          }
          if (candidates.isEmpty()) {
            return Optional.empty();
          }
          throw new IllegalStateException(
              "Ambiguous class-diagram imports for used type '"
                  + simpleName
                  + "': "
                  + candidates.stream().sorted().collect(java.util.stream.Collectors.joining(", ")));
        });
  }

  private static boolean classpathTypeExists(String qualifiedName) {
    try {
      Class.forName(qualifiedName, false, ConcreteSymbolResolver.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException | LinkageError ignored) {
      return false;
    }
  }

  String resolveConcreteCdType(String type) {
    return qualifyCdType(concreteType(type));
  }

  String replaceConcreteWithGroupingType(String rawType) {
    if (rawType == null
        || rawType.isEmpty()
        || handler.incarnationContext == null
        || !handler.useCommonParentForMultipleIncarnations) {
      return rawType;
    }
    return JavaSourceNames.replaceSimpleTypeNames(
        rawType,
        simple -> {
          Optional<String> direct = groupingName(simple);
          if (direct.isPresent()) {
            return direct;
          }
          for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry :
              handler.incarnationContext.getMappings().entrySet()) {
            if (entry.getKey().getName().equals(simple) && !entry.getValue().isEmpty()) {
              return groupingName(entry.getValue().get(0).key().getName());
            }
          }
          return Optional.empty();
        });
  }

  private List<String> normalizedParameterTypes(ASTCDMethod method) {
    return method.getCDParameterList().stream()
        .map(ASTCDParameter::getMCType)
        .map(JavaSourceNames::printNormalizedType)
        .toList();
  }

  private String concreteType(String type) {
    return JavaSourceNames.replaceSimpleTypeNames(
        type,
        simple -> {
          String concrete = resolveConcreteTypeName(simple);
          return concrete.equals(simple) ? Optional.empty() : Optional.of(concrete);
        });
  }

  private Optional<ISymbol> findCommonParent(List<ISymbol> incarnations) {
    Set<String> names = new HashSet<>();
    incarnations.forEach(symbol -> names.add(symbol.getName()));
    for (String possibleGrouping : names) {
      Optional<IncarnationContext.MappedElement> groupingElement =
              handler.incarnationContext.getGroupingMappings().values().stream()
                  .filter(element -> element.key().getName().equals(possibleGrouping))
                  .findFirst();
      if (groupingElement.isEmpty()) {
        continue;
      }
      Set<String> implementers =
          handler.incarnationContext.getGroupingMappings().entrySet().stream()
              .filter(entry -> entry.getValue().key().getName().equals(possibleGrouping))
              .map(entry -> entry.getKey().getName())
              .filter(name -> !name.equals(possibleGrouping))
              .collect(java.util.stream.Collectors.toSet());
      Set<String> remaining = new HashSet<>(names);
      remaining.remove(possibleGrouping);
      if (remaining.equals(implementers)) {
        return Optional.of(groupingElement.get().symbol());
      }
    }
    return Optional.empty();
  }

  private Optional<ISymbol> findContextSymbolByName(String name) {
    Optional<ISymbol> grouping =
        handler.incarnationContext.getGroupingMappings().values().stream()
            .filter(element -> element.key().getName().equals(name))
            .map(element -> element.symbol())
            .findFirst();
    if (grouping.isPresent()) {
      return grouping;
    }
    for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry :
        handler.incarnationContext.getMappings().entrySet()) {
      Optional<ISymbol> incarnation =
          entry.getValue().stream()
              .filter(element -> element.key().getName().equals(name))
              .map(element -> element.symbol())
              .findFirst();
      if (incarnation.isPresent()) {
        return incarnation;
      }
    }
    return Optional.empty();
  }

  private Optional<String> groupingName(String concreteTypeName) {
    return handler.incarnationContext
        .getGroupingFor(StableElementKey.type(concreteTypeName))
        .map(grouping -> grouping.key().getName());
  }

  private static Map<String, Set<String>> importedTypes(
      ASTCDCompilationUnit primary, ASTCDCompilationUnit fallback) {
    Map<String, Set<String>> imports = importedTypes(fallback);
    // Generated signatures describe concrete declarations, so concrete imports intentionally win
    // over same-named reference imports. Ambiguities within the selected CD remain detectable.
    importedTypes(primary).forEach(imports::put);
    return imports;
  }

  private static Map<String, Set<String>> importedTypes(ASTCDCompilationUnit cd) {
    Map<String, Set<String>> imports = new LinkedHashMap<>();
    if (cd == null) {
      return imports;
    }
    for (var statement : cd.getMCImportStatementList()) {
      String imported = statement.getMCQualifiedName().getQName();
      if (!statement.isStar()) {
        imports
            .computeIfAbsent(
                JavaSourceNames.simpleName(imported), ignored -> new LinkedHashSet<>())
            .add(imported);
      }
    }
    return imports;
  }

  private static Set<String> wildcardImportPackages(
      ASTCDCompilationUnit primary, ASTCDCompilationUnit fallback) {
    LinkedHashSet<String> packages = new LinkedHashSet<>(wildcardImportPackages(fallback));
    packages.addAll(wildcardImportPackages(primary));
    return Set.copyOf(packages);
  }

  private static Set<String> wildcardImportPackages(ASTCDCompilationUnit cd) {
    if (cd == null) {
      return Set.of();
    }
    return cd.getMCImportStatementList().stream()
        .filter(statement -> statement.isStar())
        .map(statement -> statement.getMCQualifiedName().getQName())
        .collect(
            java.util.stream.Collectors.toCollection(LinkedHashSet::new));
  }
}
