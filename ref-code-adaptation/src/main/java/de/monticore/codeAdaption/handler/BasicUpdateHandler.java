package de.monticore.codeAdaption.handler;

import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.multiIncarnation.IncarnationContext;
import de.monticore.codeAdaption.handler.multiIncarnation.StableElementKey;
import de.monticore.codeAdaption.matcher.CodeMatching;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.utils.CDModelIndex;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Coordinates the CD-based adaptation of Java AST elements. */
public final class BasicUpdateHandler {
  final CDModelIndex conIndex;
  final CDModelIndex inputConIndex;
  final CDModelIndex refIndex;
  final CDConformanceChecker checker;
  final CodeUpdater updater;
  final CodeValidator validator;

  /** Optional incarnation context for stereotype-based mapping when conformance is skipped. */
  final IncarnationContext incarnationContext;

  /** Stable concrete choices for the current isolated pass. Empty for the ordinary case. */
  final Map<StableElementKey, IncarnationContext.MappedElement> incarnationSelection;

  final boolean useCommonParentForMultipleIncarnations;

  private final ConcreteSymbolResolver symbolResolver;
  private final JavaMemberUpdateService memberUpdates;
  private final JavaTypeUpdateService typeUpdates;

  public BasicUpdateHandler(
      CDModelIndex refIndex,
      CDModelIndex conIndex,
      CDModelIndex inputConIndex,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationContext incarnationContext,
      Map<StableElementKey, IncarnationContext.MappedElement> incarnationSelection,
      boolean useCommonParentForMultipleIncarnations) {
    this.updater = updater;
    this.checker = checker;
    this.conIndex = conIndex;
    this.inputConIndex = inputConIndex;
    this.refIndex = refIndex;
    this.validator = validator;
    this.incarnationContext = incarnationContext;
    this.incarnationSelection = Map.copyOf(incarnationSelection);
    this.useCommonParentForMultipleIncarnations = useCommonParentForMultipleIncarnations;
    this.symbolResolver =
        new ConcreteSymbolResolver(this, refIndex.cd(), conIndex.cd());
    this.memberUpdates = new JavaMemberUpdateService(this, symbolResolver);
    this.typeUpdates = new JavaTypeUpdateService(this, symbolResolver, memberUpdates);
  }

  public void handleUpdate(Set<ASTOrdinaryCompilationUnit> javaFiles) {
    validateIncarnationContext();
    typeUpdates.beginRun();
    validator.initializeTypeMatcher(javaFiles);

    Set<JavaAstElemCollector> typeElements = new LinkedHashSet<>();
    for (ASTOrdinaryCompilationUnit ast : javaFiles) {
      JavaAstElemCollector collector = new JavaAstElemCollector();
      JavaDSLTraverser traverser = JavaDSLMill.traverser();
      traverser.add4JavaDSL(collector);
      ast.accept(traverser);
      typeElements.add(collector);
    }

    typeElements.forEach(memberUpdates::handleVariableUpdate);
    typeElements.forEach(memberUpdates::handleMemberUpdate);
    typeElements.forEach(memberUpdates::handleAssociationRoleUpdate);
    typeElements.forEach(typeUpdates::handleTypeUpdate);
    typeElements.forEach(typeUpdates::projectCompletedMembers);
    updater.printCode();
  }

  private void validateIncarnationContext() {
    if (incarnationContext == null) {
      return;
    }
    for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry :
        incarnationContext.getMappings().entrySet()) {
      if (entry.getKey().getKind() == StableElementKey.Kind.METHOD
          && entry.getValue().stream().map(value -> value.key().signature()).distinct().count()
              != entry.getValue().size()) {
        throw new IllegalStateException(
            "Conflicting method incarnations for " + entry.getKey().signature());
      }
      if (entry.getKey().getKind() != StableElementKey.Kind.FIELD) {
        continue;
      }
      Map<String, String> ownersAndTypes = new LinkedHashMap<>();
      for (IncarnationContext.MappedElement mapped : entry.getValue()) {
        StableElementKey target = mapped.key();
        String ownerAndName = target.getOwnerType().orElse("") + "." + target.getName();
        String fieldKind = target.getFieldKind().orElse("");
        String previous = ownersAndTypes.putIfAbsent(ownerAndName, fieldKind);
        if (previous != null && !previous.equals(fieldKind)) {
          throw new IllegalStateException(
              "Conflicting field incarnations for " + entry.getKey().signature());
        }
      }
    }
  }

  /** Builds the concrete name for an element from the references in its matching. */
  String buildConcreteName(CodeMatching matching) {
    return symbolResolver.buildConcreteName(matching);
  }

  /** Resolves a reference symbol from the optional incarnation context. */
  Optional<ISymbol> getSymbolFromContext(ISymbol refSymbol) {
    return symbolResolver.getSymbolFromContext(refSymbol);
  }

  Optional<StableElementKey> referenceKey(ISymbol symbol) {
    return StableElementKey.fromSymbol(symbol, refIndex);
  }

  Optional<StableElementKey> concreteKey(ISymbol symbol) {
    return StableElementKey.fromSymbol(symbol, conIndex);
  }

  List<IncarnationContext.MappedElement> mappedIncarnations(ISymbol reference) {
    if (incarnationContext == null) {
      return List.of();
    }
    return referenceKey(reference)
        .map(incarnationContext::getIncarnations)
        .orElseGet(List::of);
  }

  ISymbol getConTypeSymbol(CDTypeSymbol symbol) {
    return symbolResolver.getConTypeSymbol(symbol);
  }

  ISymbol getConAttributeSymbol(FieldSymbol symbol) {
    return symbolResolver.getConAttributeSymbol(symbol);
  }

  ISymbol getConMethodSymbol(ISymbol symbol) {
    return symbolResolver.getConMethodSymbol(symbol);
  }
}
