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
    this.incarnationSelector = Objects.requireNonNull(incarnationSelector, "incarnationSelector");
    if (mappingContexts == null || mappingContexts.isEmpty()) {
      throw new IllegalArgumentException("At least one incarnation context is required");
    }
    this.currentMapping = mappingContexts.keySet().stream().sorted().findFirst().orElseThrow();
    this.incarnationContext = mappingContexts.get(this.currentMapping);
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
    this.incarnationSelector = Objects.requireNonNull(incarnationSelector, "incarnationSelector");
    this.currentMapping = Objects.requireNonNull(mappingName, "mappingName");
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

  /** Collects incarnations from the current mapping only. */
  private List<ISymbol> getAllIncarnations(ISymbol referenceSymbol) {
    if (incarnationContext == null) {
      return List.of();
    }
    return mappedIncarnations(referenceSymbol).stream()
        .map(IncarnationContext.MappedElement::symbol)
        .toList();
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

    List<ISymbol> incarnations = getAllIncarnations(refSymbol);
    if (incarnations.isEmpty()) {
      return Optional.empty();
    }
    if (incarnations.size() == 1) {
      return Optional.of(incarnations.get(0));
    }

    IncarnationSelector.SelectionContext ctx =
        new IncarnationSelector.SelectionContext(getCurrentMapping(), refSymbol);
    ISymbol selected = incarnationSelector.selectIncarnation(refSymbol, incarnations, ctx);
    if (selected != null) {
      return Optional.of(selected);
    }

    if (refSymbol instanceof CDTypeSymbol && useCommonParentForMultipleIncarnations) {
      Optional<ISymbol> commonParent = findCommonParentInIncarnations(incarnations);
      if (commonParent.isPresent()) {
        return commonParent;
      }
      for (ISymbol incarnation : incarnations) {
        var grouping = groupingName(incarnation.getName());
        if (grouping.isPresent()) {
          Optional<ISymbol> groupingSymbol = findContextSymbolByName(grouping.get());
          if (groupingSymbol.isPresent()) {
            return groupingSymbol;
          }
        }
      }
    }
    // Fallback to first if selector couldn't decide
    return Optional.of(incarnations.get(0));
  }

  private Optional<ISymbol> findContextSymbolByName(String name) {
    if (name == null || incarnationContext == null) {
      return Optional.empty();
    }
    Optional<ISymbol> grouping =
        incarnationContext.getGroupingMappings().values().stream()
            .filter(element -> element.key().getName().equals(name))
            .map(IncarnationContext.MappedElement::symbol)
            .findFirst();
    if (grouping.isPresent()) {
      return grouping;
    }
    for (List<IncarnationContext.MappedElement> incarnations :
        incarnationContext.getMappings().values()) {
      for (IncarnationContext.MappedElement incarnation : incarnations) {
        if (incarnation.key().getName().equals(name)) {
          return Optional.of(incarnation.symbol());
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
    for (String parentName : incarnationNames) {
      Optional<IncarnationContext.MappedElement> grouping =
          incarnationContext.getGroupingMappings().values().stream()
              .filter(element -> element.key().getName().equals(parentName))
              .findFirst();
      if (grouping.isEmpty()) {
        continue;
      }
      Set<String> implementerNames =
          incarnationContext.getGroupingMappings().entrySet().stream()
              .filter(entry -> entry.getValue().key().getName().equals(parentName))
              .map(entry -> entry.getKey().getName())
              .filter(name -> !name.equals(parentName))
              .collect(java.util.stream.Collectors.toSet());
      Set<String> withoutParent = new HashSet<>(incarnationNames);
      withoutParent.remove(parentName);
      if (withoutParent.equals(implementerNames)) {
        return Optional.of(grouping.get().symbol());
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
    for (Map.Entry<StableElementKey, List<IncarnationContext.MappedElement>> entry :
        incarnationContext.getMappings().entrySet()) {
      if (entry.getKey().getKind() == StableElementKey.Kind.METHOD && entry.getValue().size() > 1) {
        Set<String> signatures = new LinkedHashSet<>();
        for (IncarnationContext.MappedElement target : entry.getValue()) {
          signatures.add(target.key().signature());
        }
        if (signatures.size() != entry.getValue().size()) {
          throw new IllegalStateException(
              "Conflicting method incarnations for " + entry.getKey().signature());
        }
      }
      if (entry.getKey().getKind() == StableElementKey.Kind.FIELD && entry.getValue().size() > 1) {
        Map<String, String> ownersAndTypes = new LinkedHashMap<>();
        for (IncarnationContext.MappedElement mapped : entry.getValue()) {
          StableElementKey target = mapped.key();
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

  private Optional<String> groupingName(String concreteTypeName) {
    return incarnationContext
        .getGroupingFor(StableElementKey.type(concreteTypeName))
        .map(grouping -> grouping.key().getName());
  }

}
