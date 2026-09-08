package de.monticore.codeAdaption.dependency;

import de.monticore.codeAdaption.updater.spoonUpdater.SpoonReferenceCodeDependencyAnalyzer;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * Facade for selecting the transitive Java-only helper closure of mapped
 * reference types.
 *
 * <p>The complete source graph is built once. Call {@link #select(Set)} for each mapping-specific
 * set of mapped Java roots. Parser-specific implementation objects never escape this facade.
 */
public final class ReferenceCodeDependencySelector {
  private final SpoonReferenceCodeDependencyAnalyzer analyzer;

  /**
   * Builds a dependency graph from one immutable reference-source snapshot.
   *
   * @param sourceRoot root containing the same Java files represented by {@code parsedUnits}
   * @param parsedUnits JavaDSL AST snapshot also supplied to helper-aware validation
   * @param referenceCDTypeKeys simple reference-CD type names used to plan incarnation passes
   */
  public ReferenceCodeDependencySelector(
      Path sourceRoot,
      Set<ASTOrdinaryCompilationUnit> parsedUnits,
      Set<String> referenceCDTypeKeys) {
    Objects.requireNonNull(sourceRoot, "sourceRoot");
    Objects.requireNonNull(parsedUnits, "parsedUnits");
    Objects.requireNonNull(referenceCDTypeKeys, "referenceCDTypeKeys");
    analyzer =
        new SpoonReferenceCodeDependencyAnalyzer(
            sourceRoot, parsedUnits, referenceCDTypeKeys);
  }

  /**
   * Selects mapped roots and every transitively required source-local helper.
   *
   * @param mappedRootTypeIdentities package-qualified names are preferred; an unqualified name is
   *     accepted only when it identifies exactly one source type
   * @throws de.monticore.codeAdaption.CodeAdaptationException if a root or reachable source-local
   *     dependency is ambiguous
   */
  public ReferenceCodeSelection select(Set<String> mappedRootTypeIdentities) {
    return analyzer.select(mappedRootTypeIdentities);
  }
}
