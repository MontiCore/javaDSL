package de.monticore.codeAdaption.updater.spoonUpdater;

import de.monticore.codeAdaption.CodeAdaptationException;
import de.monticore.codeAdaption.dependency.ReferenceCodeSelection;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

/** Spoon implementation behind {@code ReferenceCodeDependencySelector}. */
public final class SpoonReferenceCodeDependencyAnalyzer {
  private final Map<String, SourceTypeNode> graph;
  private final Map<String, Set<String>> identitiesBySimpleName;

  public SpoonReferenceCodeDependencyAnalyzer(
      Path sourceRoot,
      Set<ASTOrdinaryCompilationUnit> parsedUnits,
      Set<String> referenceCDTypeKeys) {
    Path normalizedRoot = sourceRoot.toAbsolutePath().normalize();
    if (!Files.isDirectory(normalizedRoot)) {
      throw new IllegalArgumentException(
          "Reference source directory does not exist: " + normalizedRoot);
    }

    Set<String> normalizedCDTypeKeys = normalizeSimpleNames(referenceCDTypeKeys);
    Map<String, UnitContext> contexts = sourceContexts(parsedUnits);
    identitiesBySimpleName = indexSimpleNames(contexts.keySet());
    graph =
        buildGraph(normalizedRoot, contexts, normalizedCDTypeKeys, identitiesBySimpleName);
  }

  public ReferenceCodeSelection select(Set<String> mappedRootTypeIdentities) {
    Objects.requireNonNull(mappedRootTypeIdentities, "mappedRootTypeIdentities");
    Set<String> roots = new TreeSet<>();
    for (String requestedRoot : new TreeSet<>(mappedRootTypeIdentities)) {
      roots.add(resolveRoot(requestedRoot));
    }

    Set<String> selected = new TreeSet<>();
    Deque<String> work = new ArrayDeque<>(roots);
    while (!work.isEmpty()) {
      String identity = work.removeFirst();
      if (!selected.add(identity)) {
        continue;
      }
      SourceTypeNode node = graph.get(identity);
      if (node == null) {
        throw new CodeAdaptationException(
            "Selected reference source type is absent from the dependency graph: '"
                + identity
                + "'");
      }
      if (!node.diagnostics().isEmpty()) {
        throw new CodeAdaptationException(String.join(System.lineSeparator(), node.diagnostics()));
      }
      work.addAll(node.dependencies());
    }

    Map<String, Set<String>> selectedEdges = new TreeMap<>();
    Map<String, Set<String>> selectedCDKeys = new TreeMap<>();
    for (String identity : selected) {
      SourceTypeNode node = graph.get(identity);
      Set<String> retainedDependencies = new TreeSet<>(node.dependencies());
      retainedDependencies.retainAll(selected);
      selectedEdges.put(identity, retainedDependencies);
      selectedCDKeys.put(identity, node.referenceCDTypeKeys());
    }

    Set<String> helpers = new TreeSet<>(selected);
    helpers.removeAll(roots);
    return new ReferenceCodeSelection(
        roots, helpers, selectedEdges, selectedCDKeys, List.of());
  }

  private String resolveRoot(String requestedRoot) {
    String normalized = requestedRoot == null ? "" : requestedRoot.trim();
    if (graph.containsKey(normalized)) {
      return normalized;
    }
    String simpleName = simpleName(normalized);
    Set<String> candidates = identitiesBySimpleName.getOrDefault(simpleName, Set.of());
    if (candidates.size() == 1) {
      return candidates.iterator().next();
    }
    if (candidates.isEmpty()) {
      throw new CodeAdaptationException(
          "Mapped reference Java root '" + requestedRoot + "' does not exist");
    }
    throw new CodeAdaptationException(
        "Mapped reference Java root '"
            + requestedRoot
            + "' is ambiguous; candidates are "
            + candidates);
  }

  private static Map<String, SourceTypeNode> buildGraph(
      Path sourceRoot,
      Map<String, UnitContext> contexts,
      Set<String> referenceCDTypeKeys,
      Map<String, Set<String>> identitiesBySimpleName) {
    Launcher launcher = new Launcher();
    launcher.getEnvironment().setNoClasspath(true);
    launcher.addInputResource(sourceRoot.toString());
    try {
      launcher.buildModel();
    } catch (RuntimeException exception) {
      throw new CodeAdaptationException(
          "Could not analyze reference Java dependencies in '" + sourceRoot + "'", exception);
    }
    CtModel model = launcher.getModel();

    Map<String, CtType<?>> spoonTypes = new TreeMap<>();
    for (CtType<?> type : model.getAllTypes()) {
      if (type.isTopLevel() && contexts.containsKey(type.getQualifiedName())) {
        spoonTypes.put(type.getQualifiedName(), type);
      }
    }

    Set<String> missing = new TreeSet<>(contexts.keySet());
    missing.removeAll(spoonTypes.keySet());
    if (!missing.isEmpty()) {
      throw new CodeAdaptationException(
          "Spoon did not model reference Java top-level types " + missing);
    }

    Map<String, SourceTypeNode> result = new TreeMap<>();
    for (Map.Entry<String, CtType<?>> entry : spoonTypes.entrySet()) {
      String owner = entry.getKey();
      UnitContext context = contexts.get(owner);
      Set<String> dependencies = new TreeSet<>();
      Set<String> cdTypes = new TreeSet<>();
      Set<String> diagnostics = new TreeSet<>();

      List<CtTypeReference<?>> references =
          new ArrayList<>(
              entry.getValue().getElements(new TypeFilter<>(CtTypeReference.class)));
      references.sort(Comparator.comparing(SpoonReferenceCodeDependencyAnalyzer::referenceKey));
      for (CtTypeReference<?> reference : references) {
        String referencedSimpleName = reference.getSimpleName();
        if (referenceCDTypeKeys.contains(referencedSimpleName)) {
          cdTypes.add(referencedSimpleName);
        }
        Resolution resolution =
            resolveSourceType(reference, context, contexts.keySet(), identitiesBySimpleName);
        if (resolution.diagnostic().isPresent()) {
          diagnostics.add(
              "Unresolved or ambiguous source-local type reference '"
                  + referenceKey(reference)
                  + "' in '"
                  + owner
                  + "': "
                  + resolution.diagnostic().get());
        }
        resolution
            .identity()
            .filter(identity -> !identity.equals(owner))
            .ifPresent(
                identity -> {
                  Optional<String> visibilityDiagnostic =
                      topLevelVisibilityDiagnostic(owner, identity, contexts, spoonTypes);
                  if (visibilityDiagnostic.isPresent()) {
                    diagnostics.add(visibilityDiagnostic.get());
                  } else {
                    dependencies.add(identity);
                  }
                });
      }
      result.put(
          owner,
          new SourceTypeNode(
              Collections.unmodifiableSet(dependencies),
              Collections.unmodifiableSet(cdTypes),
              List.copyOf(diagnostics)));
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(result));
  }

  private static Optional<String> topLevelVisibilityDiagnostic(
      String owner,
      String dependency,
      Map<String, UnitContext> contexts,
      Map<String, CtType<?>> spoonTypes) {
    UnitContext ownerContext = contexts.get(owner);
    UnitContext dependencyContext = contexts.get(dependency);
    CtType<?> dependencyType = spoonTypes.get(dependency);
    if (ownerContext == null || dependencyContext == null || dependencyType == null) {
      return Optional.empty();
    }
    if (ownerContext.packageName().equals(dependencyContext.packageName())
        || dependencyType.hasModifier(ModifierKind.PUBLIC)) {
      return Optional.empty();
    }
    return Optional.of(
        "Source-local top-level type '"
            + dependency
            + "' referenced from '"
            + owner
            + "' is not accessible: it is package-private in package '"
            + dependencyContext.packageName()
            + "', but the referencing type is in package '"
            + ownerContext.packageName()
            + "'");
  }

  private static Resolution resolveSourceType(
      CtTypeReference<?> reference,
      UnitContext context,
      Set<String> sourceIdentities,
      Map<String, Set<String>> identitiesBySimpleName) {
    String simpleName = reference.getSimpleName();

    if (simpleName == null || simpleName.isBlank()) {
      return Resolution.unresolved();
    }

    if (!isSimplyQualified(reference)) {
      Optional<String> declarationOwner = resolvedDeclarationOwner(reference, sourceIdentities);
      if (declarationOwner.isPresent()) {
        return Resolution.resolved(declarationOwner.get());
      }
      Optional<String> qualifiedOwner =
          topLevelOwner(normalizedQualifiedName(reference), sourceIdentities);
      return qualifiedOwner.map(Resolution::resolved).orElseGet(Resolution::unresolved);
    }

    Set<String> matchingExplicitImports =
        context.explicitImports().stream()
            .filter(imported -> simpleName(imported).equals(simpleName))
            .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
    if (!matchingExplicitImports.isEmpty()) {
      Set<String> explicitSourceCandidates = new TreeSet<>(matchingExplicitImports);
      explicitSourceCandidates.retainAll(sourceIdentities);
      if (!explicitSourceCandidates.isEmpty()) {
        return uniqueOrAmbiguous(explicitSourceCandidates);
      }
      // A single-type import is an established external binding. It takes precedence over a
      // coincidental source-local declaration with the same simple name.
      return Resolution.unresolved();
    }

    String samePackage = qualify(context.packageName(), simpleName);
    if (sourceIdentities.contains(samePackage)) {
      return Resolution.resolved(samePackage);
    }

    Set<String> wildcardCandidates = new TreeSet<>();
    context.wildcardImports().stream()
        .map(packageName -> qualify(packageName, simpleName))
        .filter(sourceIdentities::contains)
        .forEach(wildcardCandidates::add);
    if (!wildcardCandidates.isEmpty()) {
      return uniqueOrAmbiguous(wildcardCandidates);
    }

    String qualifiedName = normalizedQualifiedName(reference);
    if (isQualifiedReference(reference)
        && !samePackage.equals(qualifiedName)
        && topLevelOwner(qualifiedName, sourceIdentities).isEmpty()) {
      // Spoon resolved an external/java.lang type. It is not a source dependency even when an
      // unrelated source unit happens to declare the same simple name.
      return Resolution.unresolved();
    }

    Set<String> globalCandidates = identitiesBySimpleName.getOrDefault(simpleName, Set.of());
    if (globalCandidates.size() > 1) {
      return Resolution.ambiguous(globalCandidates);
    }
    if (globalCandidates.size() == 1) {
      return Resolution.unresolvedSourceCandidate(globalCandidates.iterator().next());
    }
    return Resolution.unresolved();
  }

  private static Optional<String> resolvedDeclarationOwner(
      CtTypeReference<?> reference, Set<String> sourceIdentities) {
    try {
      CtType<?> declaration = reference.getTypeDeclaration();
      if (declaration == null) {
        return Optional.empty();
      }
      CtType<?> topLevel = declaration;
      while (topLevel.getDeclaringType() != null) {
        topLevel = topLevel.getDeclaringType();
      }
      return sourceIdentities.contains(topLevel.getQualifiedName())
          ? Optional.of(topLevel.getQualifiedName())
          : Optional.empty();
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
  }

  private static Resolution uniqueOrAmbiguous(Set<String> candidates) {
    return candidates.size() == 1
        ? Resolution.resolved(candidates.iterator().next())
        : Resolution.ambiguous(candidates);
  }

  private static Optional<String> topLevelOwner(
      String qualifiedName, Set<String> sourceIdentities) {
    if (qualifiedName == null || qualifiedName.isBlank()) {
      return Optional.empty();
    }
    String normalized = qualifiedName.replace('$', '.');
    return sourceIdentities.stream()
        .filter(identity -> normalized.equals(identity) || normalized.startsWith(identity + "."))
        .max(Comparator.comparingInt(String::length));
  }

  private static boolean isQualifiedReference(CtTypeReference<?> reference) {
    String qualified = normalizedQualifiedName(reference);
    return qualified != null
        && qualified.contains(".")
        && !qualified.equals(reference.getSimpleName());
  }

  private static boolean isSimplyQualified(CtTypeReference<?> reference) {
    try {
      return reference.isSimplyQualified();
    } catch (RuntimeException ignored) {
      return !isQualifiedReference(reference);
    }
  }

  private static String normalizedQualifiedName(CtTypeReference<?> reference) {
    try {
      return reference.getQualifiedName();
    } catch (RuntimeException ignored) {
      return reference.getSimpleName();
    }
  }

  private static String referenceKey(CtTypeReference<?> reference) {
    String qualifiedName = normalizedQualifiedName(reference);
    return qualifiedName == null ? String.valueOf(reference.getSimpleName()) : qualifiedName;
  }

  private static Map<String, UnitContext> sourceContexts(
      Set<ASTOrdinaryCompilationUnit> units) {
    Map<String, UnitContext> result = new TreeMap<>();
    for (ASTOrdinaryCompilationUnit unit : units) {
      String packageName =
          unit.isPresentPackageDeclaration()
              ? unit.getPackageDeclaration().getMCQualifiedName().getQName()
              : "";
      Set<String> explicitImports = new TreeSet<>();
      Set<String> wildcardImports = new TreeSet<>();
      for (ASTImportDeclaration importDeclaration : unit.getImportDeclarationList()) {
        String imported = importDeclaration.getMCQualifiedName().getQName();
        if (importDeclaration.isSTAR()) {
          wildcardImports.add(imported);
        } else {
          explicitImports.add(imported);
        }
      }
      UnitContext context =
          new UnitContext(
              packageName,
              Collections.unmodifiableSet(explicitImports),
              Collections.unmodifiableSet(wildcardImports));
      for (ASTTypeDeclaration type : unit.getTypeDeclarationList()) {
        String identity = qualify(packageName, type.getName());
        if (result.putIfAbsent(identity, context) != null) {
          throw new CodeAdaptationException(
              "Reference Java declares top-level type '" + identity + "' more than once");
        }
      }
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(result));
  }

  private static Map<String, Set<String>> indexSimpleNames(Collection<String> identities) {
    Map<String, Set<String>> index = new TreeMap<>();
    for (String identity : identities) {
      index.computeIfAbsent(simpleName(identity), ignored -> new TreeSet<>()).add(identity);
    }
    Map<String, Set<String>> result = new LinkedHashMap<>();
    index.forEach(
        (name, values) ->
            result.put(name, Collections.unmodifiableSet(new LinkedHashSet<>(values))));
    return Collections.unmodifiableMap(result);
  }

  private static Set<String> normalizeSimpleNames(Set<String> names) {
    Set<String> result = new TreeSet<>();
    names.stream().map(SpoonReferenceCodeDependencyAnalyzer::simpleName).forEach(result::add);
    return Collections.unmodifiableSet(result);
  }

  private static String qualify(String packageName, String simpleName) {
    return packageName == null || packageName.isBlank()
        ? simpleName
        : packageName + "." + simpleName;
  }

  private static String simpleName(String qualifiedName) {
    if (qualifiedName == null) {
      return "";
    }
    int separator = Math.max(qualifiedName.lastIndexOf('.'), qualifiedName.lastIndexOf('$'));
    return separator < 0 ? qualifiedName : qualifiedName.substring(separator + 1);
  }

  private record UnitContext(
      String packageName, Set<String> explicitImports, Set<String> wildcardImports) {}

  private record SourceTypeNode(
      Set<String> dependencies, Set<String> referenceCDTypeKeys, List<String> diagnostics) {}

  private record Resolution(Optional<String> identity, Optional<String> diagnostic) {
    private static Resolution resolved(String identity) {
      return new Resolution(Optional.of(identity), Optional.empty());
    }

    private static Resolution ambiguous(Set<String> candidates) {
      return new Resolution(Optional.empty(), Optional.of("candidates are " + candidates));
    }

    private static Resolution unresolved() {
      return new Resolution(Optional.empty(), Optional.empty());
    }

    private static Resolution unresolvedSourceCandidate(String candidate) {
      return new Resolution(
          Optional.empty(), Optional.of("candidate '" + candidate + "' is not imported or visible"));
    }
  }
}
