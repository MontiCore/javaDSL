package de.monticore.codeAdaption.handler;

import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cd4codebasis._ast.ASTCDParameter;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.matcher.MatcherHelper;
import de.monticore.codeAdaption.utils.JavaSourceNames;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.statements.mccommonstatements._ast.ASTFormalParameter;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Resolves reference-CD symbols, signatures, and Java type names to concrete counterparts. */
final class ConcreteSymbolResolver {
  private final BasicUpdateHandler handler;
  private final Map<String, String> concreteImportedTypes;

  ConcreteSymbolResolver(
      BasicUpdateHandler handler,
      ASTCDCompilationUnit referenceCD,
      ASTCDCompilationUnit concreteCD) {
    this.handler = handler;
    this.concreteImportedTypes = importedTypes(referenceCD);
    this.concreteImportedTypes.putAll(importedTypes(concreteCD));
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
    List<ISymbol> incarnations = handler.incarnationContext.getIncarnations(reference);
    if (incarnations == null || incarnations.isEmpty()) {
      return Optional.empty();
    }
    if (reference instanceof CDTypeSymbol && handler.useCommonParentForMultipleIncarnations) {
      Optional<ISymbol> commonParent = findCommonParent(incarnations);
      if (commonParent.isPresent()) {
        return commonParent;
      }
      for (ISymbol incarnation : incarnations) {
        Optional<String> grouping =
            handler.incarnationContext.findGroupingTypeForImplementer(incarnation.getName());
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

  void registerConcreteMethodSignature(ISymbol reference, ISymbol concrete) {
    if (concrete == null || !(concrete.getAstNode() instanceof ASTCDMethod concreteMethod)) {
      return;
    }
    if (handler.incarnationContext != null) {
      Optional<StableElementKey> referenceKey = handler.incarnationContext.getStableKey(reference);
      Optional<StableElementKey> concreteKey = handler.incarnationContext.getStableKey(concrete);
      if (referenceKey.isPresent() && concreteKey.isPresent()) {
        handler.updater.registerMethodRewrite(referenceKey.get(), concreteKey.get());
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
        List<ISymbol> alternatives =
            handler.incarnationContext.getIncarnations(referenceType.get().getSymbol());
        if (alternatives != null) {
          Optional<String> concreteAlternative =
              alternatives.stream()
                  .map(ISymbol::getName)
                  .filter(name -> !name.equals(referenceType.get().getName()))
                  .filter(handler.conIndex::hasType)
                  .sorted()
                  .findFirst();
          if (concreteAlternative.isPresent()) {
            return concreteAlternative.get();
          }
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
    return JavaSourceNames.replaceSimpleTypeNames(
        type,
        simpleName -> {
          if (handler.conIndex.type(simpleName).isPresent()
              || handler.refIndex.type(simpleName).isPresent()) {
            return Optional.empty();
          }
          return Optional.ofNullable(concreteImportedTypes.get(simpleName));
        });
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
          Optional<String> direct =
              handler.incarnationContext.findGroupingTypeForImplementer(simple);
          if (direct.isPresent()) {
            return direct;
          }
          for (Map.Entry<ISymbol, List<ISymbol>> entry :
              handler.incarnationContext.getReferenceToIncarnations().entrySet()) {
            if (entry.getKey().getName().equals(simple)
                && entry.getValue() != null
                && !entry.getValue().isEmpty()) {
              return handler.incarnationContext.findGroupingTypeForImplementer(
                  entry.getValue().get(0).getName());
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
    for (Map.Entry<ISymbol, List<ISymbol>> entry :
        handler.incarnationContext.getInterfaceToImplementers().entrySet()) {
      if (!names.contains(entry.getKey().getName())) {
        continue;
      }
      Set<String> implementers = new HashSet<>();
      entry.getValue().forEach(symbol -> implementers.add(symbol.getName()));
      Set<String> remaining = new HashSet<>(names);
      remaining.remove(entry.getKey().getName());
      if (remaining.equals(implementers)) {
        return Optional.of(entry.getKey());
      }
    }
    return Optional.empty();
  }

  private Optional<ISymbol> findContextSymbolByName(String name) {
    for (Map.Entry<ISymbol, List<ISymbol>> entry :
        handler.incarnationContext.getInterfaceToImplementers().entrySet()) {
      if (entry.getKey().getName().equals(name)) {
        return Optional.of(entry.getKey());
      }
    }
    for (Map.Entry<ISymbol, List<ISymbol>> entry :
        handler.incarnationContext.getReferenceToIncarnations().entrySet()) {
      if (entry.getKey().getName().equals(name)) {
        return Optional.of(entry.getKey());
      }
      Optional<ISymbol> incarnation =
          entry.getValue().stream().filter(symbol -> symbol.getName().equals(name)).findFirst();
      if (incarnation.isPresent()) {
        return incarnation;
      }
    }
    return Optional.empty();
  }

  private static Map<String, String> importedTypes(ASTCDCompilationUnit cd) {
    Map<String, String> imports = new LinkedHashMap<>();
    if (cd == null) {
      return imports;
    }
    for (var statement : cd.getMCImportStatementList()) {
      String imported = statement.getMCQualifiedName().getQName();
      if (!imported.endsWith(".*") && !imported.startsWith("java.lang.")) {
        imports.putIfAbsent(JavaSourceNames.simpleName(imported), imported);
      }
    }
    return imports;
  }
}
