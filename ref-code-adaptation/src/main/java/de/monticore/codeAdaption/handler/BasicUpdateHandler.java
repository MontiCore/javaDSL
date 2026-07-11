package de.monticore.codeAdaption.handler;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
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
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Coordinates the CD-based adaptation of Java AST elements. */
public class BasicUpdateHandler {
  protected CDModelIndex conIndex;
  protected CDModelIndex refIndex;
  protected CDConformanceChecker checker;
  protected CodeUpdater updater;
  protected CodeValidator validator;

  /** Optional incarnation context for stereotype-based mapping when conformance is skipped. */
  protected IncarnationContext incarnationContext;

  /** Stable concrete choices for the current isolated pass. Empty for the ordinary case. */
  final Map<StableElementKey, IncarnationContext.MappedElement> incarnationSelection;

  protected boolean useCommonParentForMultipleIncarnations;

  private final ConcreteSymbolResolver symbolResolver;
  private final JavaMemberUpdateService memberUpdates;
  private final JavaTypeUpdateService typeUpdates;

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
    this(refCD, conCD, conHwcPath, checker, updater, validator, incarnationContext, Map.of(), true);
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
    this(
        refCD,
        conCD,
        conHwcPath,
        checker,
        updater,
        validator,
        incarnationContext,
        Map.of(),
        useCommonParentForMultipleIncarnations);
  }

  public BasicUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationContext incarnationContext,
      Map<StableElementKey, IncarnationContext.MappedElement> incarnationSelection,
      boolean useCommonParentForMultipleIncarnations) {
    this.updater = updater;
    this.checker = checker;
    this.conIndex = CDModelIndex.of(conCD);
    this.refIndex = CDModelIndex.of(refCD);
    this.validator = validator;
    this.incarnationContext = incarnationContext;
    this.incarnationSelection =
        incarnationSelection == null ? Map.of() : Map.copyOf(incarnationSelection);
    this.useCommonParentForMultipleIncarnations = useCommonParentForMultipleIncarnations;
    this.symbolResolver = new ConcreteSymbolResolver(this, refCD, conCD);
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

    typeElements.forEach(this::handleVariableUpdate);
    typeElements.forEach(this::handleTMemberUpdate);
    typeElements.forEach(memberUpdates::handleAssociationRoleUpdate);
    typeElements.forEach(this::handleTypeUpdate);
    typeElements.forEach(this::projectCompletedMembers);
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

  /** Compatibility hook retained for specialized handlers. */
  protected void handleTypeUpdate(JavaAstElemCollector collector) {
    typeUpdates.handleTypeUpdate(collector);
  }

  /** Compatibility hook retained for specialized handlers. */
  protected void handleTMemberUpdate(JavaAstElemCollector collector) {
    memberUpdates.handleMemberUpdate(collector);
  }

  /** Compatibility hook retained for specialized handlers. */
  protected void projectCompletedMembers(JavaAstElemCollector collector) {
    typeUpdates.projectCompletedMembers(collector);
  }

  /** Compatibility hook retained for specialized handlers. */
  protected void handleVariableUpdate(JavaAstElemCollector collector) {
    memberUpdates.handleVariableUpdate(collector);
  }

  /** Compatibility hook retained for callers that explicitly request the fallback mapping. */
  protected void updateMethodParametersFromConcreteCD(
      ASTTypeDeclaration type, ASTMethodDeclaration method, JavaAstElemCollector collector) {
    memberUpdates.updateMethodParametersFromConcreteCD(type, method, collector, Set.of());
  }

  /** Builds the concrete name for an element from the references in its matching. */
  protected String buildConcreteName(CodeMatching matching) {
    return symbolResolver.buildConcreteName(matching);
  }

  /** Resolves a reference symbol from the optional incarnation context. */
  protected Optional<ISymbol> getSymbolFromContext(ISymbol refSymbol) {
    return symbolResolver.getSymbolFromContext(refSymbol);
  }

  protected Optional<StableElementKey> referenceKey(ISymbol symbol) {
    return StableElementKey.fromSymbol(symbol, refIndex);
  }

  protected Optional<StableElementKey> concreteKey(ISymbol symbol) {
    return StableElementKey.fromSymbol(symbol, conIndex);
  }

  protected List<IncarnationContext.MappedElement> mappedIncarnations(ISymbol reference) {
    if (incarnationContext == null) {
      return List.of();
    }
    return referenceKey(reference)
        .map(incarnationContext::getIncarnations)
        .orElseGet(List::of);
  }

  /** Default conformance-based type resolution; subclasses may select another incarnation. */
  protected ISymbol getConTypeSymbol(CDTypeSymbol symbol) {
    return symbolResolver.getConTypeSymbol(symbol);
  }

  /** Default conformance-based field resolution; subclasses may select another incarnation. */
  protected ISymbol getConAttributeSymbol(FieldSymbol symbol) {
    return symbolResolver.getConAttributeSymbol(symbol);
  }

  /** Default conformance-based method resolution. */
  protected ISymbol getConMethodSymbol(ISymbol symbol) {
    return symbolResolver.getConMethodSymbol(symbol);
  }
}
