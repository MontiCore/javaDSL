package de.monticore.codeAdaption.handler.multiIncarnation;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.codeAdaption.handler.BasicUpdateHandler;
import de.monticore.codeAdaption.updater.CodeUpdater;
import de.monticore.codeAdaption.validator.CodeValidator;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symboltable.ISymbol;
import java.nio.file.Path;
import java.util.*;

/**
 * Extended update handler that supports multiple incarnations of the same pattern.
 * This handler can handle cases where the same reference element has multiple concrete incarnations.
 */
public class MultiIncarnationUpdateHandler extends BasicUpdateHandler {
  private final IncarnationSelector incarnationSelector;
  private final Map<String, IncarnationContext> mappingContexts;
  private String currentMapping;

  public MultiIncarnationUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationSelector incarnationSelector,
      Map<String, IncarnationContext> mappingContexts) {
    this(
        refCD,
        conCD,
        conHwcPath,
        checker,
        updater,
        validator,
        incarnationSelector,
        mappingContexts,
        true);
  }

  public MultiIncarnationUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationSelector incarnationSelector,
      Map<String, IncarnationContext> mappingContexts,
      boolean useCommonParentForMultipleIncarnations) {

    super(
        refCD,
        conCD,
        conHwcPath,
        checker,
        updater,
        validator,
        null,
        useCommonParentForMultipleIncarnations);
    this.incarnationSelector = incarnationSelector;
    this.mappingContexts = mappingContexts;
    this.currentMapping = mappingContexts.keySet().iterator().next();
  }

  /**
   * Constructor that accepts a specific incarnation context and mapping name.
   * This allows the handler to resolve reference symbols using the provided
   * incarnation context when performing per-mapping/per-incarnation adaptations.
   */
  public MultiIncarnationUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationSelector incarnationSelector,
      Map<String, IncarnationContext> mappingContexts,
      IncarnationContext incarnationContext,
      String mappingName) {
    this(
        refCD,
        conCD,
        conHwcPath,
        checker,
        updater,
        validator,
        incarnationSelector,
        mappingContexts,
        incarnationContext,
        mappingName,
        true);
  }

  public MultiIncarnationUpdateHandler(
      ASTCDCompilationUnit refCD,
      ASTCDCompilationUnit conCD,
      Path conHwcPath,
      CDConformanceChecker checker,
      CodeUpdater updater,
      CodeValidator validator,
      IncarnationSelector incarnationSelector,
      Map<String, IncarnationContext> mappingContexts,
      IncarnationContext incarnationContext,
      String mappingName,
      boolean useCommonParentForMultipleIncarnations) {

    // Pass the specific incarnation context to the parent so getSymbolFromContext works
    super(
        refCD,
        conCD,
        conHwcPath,
        checker,
        updater,
        validator,
        incarnationContext,
        useCommonParentForMultipleIncarnations);
    this.incarnationSelector = incarnationSelector;
    this.mappingContexts = mappingContexts;
    this.currentMapping = mappingName;
  }

  @Override
  protected ISymbol getConTypeSymbol(CDTypeSymbol symbol) {
    // Get all available incarnations for this reference symbol
    List<ISymbol> incarnations = getAllIncarnations(symbol);

    if (incarnations.isEmpty()) {
      return super.getConTypeSymbol(symbol);
    }

    if (incarnations.size() == 1) {
      return incarnations.get(0);
    }

    // Multiple incarnations available - use selector
    IncarnationSelector.SelectionContext context =
        new IncarnationSelector.SelectionContext(getCurrentMapping(), symbol);
    ISymbol selected = incarnationSelector.selectIncarnation(symbol, incarnations, context);

    return selected != null ? selected : super.getConTypeSymbol(symbol);
  }

  @Override
  protected ISymbol getConAttributeSymbol(FieldSymbol symbol) {
    // Get all available incarnations for this reference symbol
    List<ISymbol> incarnations = getAllIncarnations(symbol);

    if (incarnations.isEmpty()) {
      return super.getConAttributeSymbol(symbol);
    }

    if (incarnations.size() == 1) {
      return incarnations.get(0);
    }

    // Multiple incarnations available - use selector
    IncarnationSelector.SelectionContext context =
        new IncarnationSelector.SelectionContext(getCurrentMapping(), symbol);
    ISymbol selected = incarnationSelector.selectIncarnation(symbol, incarnations, context);

    return selected != null ? selected : super.getConAttributeSymbol(symbol);
  }

  /**
   * Collects all incarnations for a given reference symbol across all mappings.
   */
  private List<ISymbol> getAllIncarnations(ISymbol referenceSymbol) {
    List<ISymbol> allIncarnations = new ArrayList<>();

    for (IncarnationContext context : mappingContexts.values()) {
      List<ISymbol> incarnations = context.getIncarnations(referenceSymbol);
      if (incarnations != null) {
        allIncarnations.addAll(incarnations);
      }
    }

    return allIncarnations;
  }

  /**
   * Gets the current mapping being processed.
   */
  public String getCurrentMapping() {
    return currentMapping;
  }

  /**
   * Sets the current mapping being processed.
   */
  public void setCurrentMapping(String mapping) {
    this.currentMapping = mapping;
  }

  @Override
  protected Optional<ISymbol> getSymbolFromContext(ISymbol refSymbol) {
    // Prefer using the specific incarnation context (set via constructor) to resolve
    // reference symbols. Use the incarnation selector when multiple incarnations exist.
    if (this.incarnationContext == null) {
      return Optional.empty();
    }

    List<ISymbol> incarnations = this.incarnationContext.getIncarnations(refSymbol);
    if (incarnations == null || incarnations.isEmpty()) {
      return Optional.empty();
    }
    if (refSymbol instanceof CDTypeSymbol && useCommonParentForMultipleIncarnations) {
      Optional<ISymbol> commonParent = findCommonParentInIncarnations(incarnations);
      if (commonParent.isPresent()) {
        return commonParent;
      }
      for (ISymbol incarnation : incarnations) {
        var grouping = this.incarnationContext.findGroupingTypeForImplementer(incarnation.getName());
        if (grouping.isPresent()) {
          Optional<ISymbol> groupingSymbol = findContextSymbolByName(grouping.get());
          if (groupingSymbol.isPresent()) {
            return groupingSymbol;
          }
        }
      }
    }
    if (incarnations.size() == 1) {
      return Optional.of(incarnations.get(0));
    }

    // Use selector to pick the incarnation appropriate for the current mapping/selection
    IncarnationSelector.SelectionContext ctx = new IncarnationSelector.SelectionContext(getCurrentMapping(), refSymbol);
    ISymbol selected = incarnationSelector.selectIncarnation(refSymbol, incarnations, ctx);
    if (selected != null) return Optional.of(selected);

    // Fallback to first if selector couldn't decide
    return Optional.of(incarnations.get(0));
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

  /**
   * Enhanced handleUpdate that performs conflict resolution before applying adaptations.
   */
  @Override
  public void handleUpdate(Set<ASTOrdinaryCompilationUnit> javaFiles) {
    validateResolvedContext();
    super.handleUpdate(javaFiles);
  }

  private void validateResolvedContext() {
    if (incarnationContext == null) {
      return;
    }
    for (Map.Entry<StableElementKey, List<StableElementKey>> entry :
        incarnationContext.getStableMappings().entrySet()) {
      if (entry.getKey().getKind() == StableElementKey.Kind.METHOD && entry.getValue().size() > 1) {
        Set<String> signatures = new LinkedHashSet<>();
        for (StableElementKey target : entry.getValue()) {
          signatures.add(target.signature());
        }
        if (signatures.size() != entry.getValue().size()) {
          throw new IllegalStateException(
              "Conflicting method incarnations for " + entry.getKey().signature());
        }
      }
      if (entry.getKey().getKind() == StableElementKey.Kind.FIELD && entry.getValue().size() > 1) {
        Map<String, String> ownersAndTypes = new LinkedHashMap<>();
        for (StableElementKey target : entry.getValue()) {
          String fieldName = target.getName();
          String fieldKind = target.getFieldKind().orElse("");
          String previous = ownersAndTypes.putIfAbsent(target.getOwnerType().orElse("") + "." + fieldName, fieldKind);
          if (previous != null && !previous.equals(fieldKind)) {
            throw new IllegalStateException(
                "Conflicting field incarnations for " + entry.getKey().signature());
          }
        }
      }
    }
  }

}
