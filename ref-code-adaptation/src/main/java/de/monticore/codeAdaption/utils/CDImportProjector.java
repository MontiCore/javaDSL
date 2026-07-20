package de.monticore.codeAdaption.utils;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Projects non-{@code java.lang} imports from reference and concrete class diagrams into
 * handwritten Java adapters.
 *
 * <p>Explicit imports are projected only into units that use their simple name. This avoids
 * creating invalid Java sources merely because unrelated reference and concrete imports have the
 * same simple name. CD wildcard imports are retained as wildcards because their exported type set
 * cannot be determined from the import declaration alone.
 */
public final class CDImportProjector {

  private CDImportProjector() {}

  /**
   * Adds required CD imports to Java compilation units without duplicating existing imports.
   *
   * <p>An existing explicit Java import is the authoritative binding for that unit. If a used,
   * otherwise-unbound simple name is supplied by multiple distinct explicit CD imports, projection
   * fails deterministically instead of creating an uncompilable pair of imports.
   *
   * @param javaUnits handwritten adapter compilation units to update
   * @param classDiagrams class diagrams whose imports are projected
   */
  public static void project(
      Set<ASTOrdinaryCompilationUnit> javaUnits, ASTCDCompilationUnit... classDiagrams) {
    Map<ImportKey, ASTImportDeclaration> imports = parseImports(classDiagrams);
    if (imports.isEmpty()) {
      return;
    }

    List<Map.Entry<ImportKey, ASTImportDeclaration>> wildcardImports =
        imports.entrySet().stream().filter(entry -> entry.getKey().wildcard()).toList();
    Map<String, List<Map.Entry<ImportKey, ASTImportDeclaration>>> explicitImports =
        explicitImportsBySimpleName(imports);

    for (ASTOrdinaryCompilationUnit unit : javaUnits) {
      Set<ImportKey> existing = new LinkedHashSet<>();
      Map<String, String> existingExplicitBindings = new LinkedHashMap<>();
      List<Map.Entry<ImportKey, ASTImportDeclaration>> importsToAdd = new ArrayList<>();
      for (ASTImportDeclaration importDeclaration : unit.getImportDeclarationList()) {
        if (importDeclaration.isStatic()) {
          continue;
        }
        ImportKey key = ImportKey.from(importDeclaration);
        existing.add(key);
        if (!key.wildcard()) {
          existingExplicitBindings.putIfAbsent(key.simpleName(), key.qualifiedName());
        }
      }

      for (Map.Entry<ImportKey, ASTImportDeclaration> wildcardImport : wildcardImports) {
        queueIfMissing(existing, importsToAdd, wildcardImport);
      }

      Set<String> declaredTypeNames =
          unit.getTypeDeclarationList().stream()
              .map(type -> type.getName())
              .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
      String sourceWithoutImports = sourceWithoutImports(unit);
      for (Map.Entry<String, List<Map.Entry<ImportKey, ASTImportDeclaration>>> candidates :
          explicitImports.entrySet()) {
        String simpleName = candidates.getKey();
        if (declaredTypeNames.contains(simpleName)
            || !usesUnqualifiedName(sourceWithoutImports, simpleName)) {
          continue;
        }

        String existingBinding = existingExplicitBindings.get(simpleName);
        if (existingBinding != null) {
          // Handwritten Java already made this binding explicit. Never add a conflicting CD import.
          continue;
        }

        List<Map.Entry<ImportKey, ASTImportDeclaration>> distinctCandidates =
            candidates.getValue().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().qualifiedName()))
                .toList();
        if (distinctCandidates.size() > 1) {
          throw ambiguousImport(simpleName, distinctCandidates);
        }
        queueIfMissing(existing, importsToAdd, distinctCandidates.get(0));
      }
      importsToAdd.forEach(candidate -> unit.addImportDeclaration(candidate.getValue().deepClone()));
    }
  }

  private static Map<String, List<Map.Entry<ImportKey, ASTImportDeclaration>>>
      explicitImportsBySimpleName(Map<ImportKey, ASTImportDeclaration> imports) {
    Map<String, List<Map.Entry<ImportKey, ASTImportDeclaration>>> bySimpleName =
        new LinkedHashMap<>();
    imports.entrySet().stream()
        .filter(entry -> !entry.getKey().wildcard())
        .forEach(
            entry ->
                bySimpleName
                    .computeIfAbsent(entry.getKey().simpleName(), ignored -> new ArrayList<>())
                    .add(entry));
    return bySimpleName;
  }

  private static void queueIfMissing(
      Set<ImportKey> existing,
      List<Map.Entry<ImportKey, ASTImportDeclaration>> importsToAdd,
      Map.Entry<ImportKey, ASTImportDeclaration> candidate) {
    if (existing.add(candidate.getKey())) {
      importsToAdd.add(candidate);
    }
  }

  private static IllegalStateException ambiguousImport(
      String simpleName,
      List<Map.Entry<ImportKey, ASTImportDeclaration>> distinctCandidates) {
    String qualifiedNames =
        distinctCandidates.stream()
            .map(entry -> entry.getKey().qualifiedName())
            .collect(java.util.stream.Collectors.joining(", "));
    return new IllegalStateException(
        "Ambiguous class-diagram imports for used Java type '"
            + simpleName
            + "': "
            + qualifiedNames);
  }

  private static String sourceWithoutImports(ASTOrdinaryCompilationUnit unit) {
    ASTOrdinaryCompilationUnit clone = unit.deepClone();
    clone.clearImportDeclarations();
    return JavaLoader.print(clone);
  }

  private static boolean usesUnqualifiedName(String source, String simpleName) {
    Pattern unqualifiedIdentifier =
        Pattern.compile(
            "(?<![\\p{Alnum}_$\\.])"
                + Pattern.quote(simpleName)
                + "(?![\\p{Alnum}_$])");
    return unqualifiedIdentifier.matcher(source).find();
  }

  private static Map<ImportKey, ASTImportDeclaration> parseImports(
      ASTCDCompilationUnit... classDiagrams) {
    Set<ImportKey> importKeys = new LinkedHashSet<>();
    for (ASTCDCompilationUnit classDiagram : classDiagrams) {
      if (classDiagram == null) {
        continue;
      }
      classDiagram.getMCImportStatementList().stream()
          .map(
              statement ->
                  new ImportKey(statement.getMCQualifiedName().getQName(), statement.isStar()))
          .filter(importKey -> !importKey.isJavaLang())
          .forEach(importKeys::add);
    }
    if (importKeys.isEmpty()) {
      return Map.of();
    }

    StringBuilder source = new StringBuilder();
    importKeys.forEach(
        importKey ->
            source
                .append("import ")
                .append(importKey.qualifiedName())
                .append(importKey.wildcard() ? ".*" : "")
                .append(";\n"));
    source.append("class CDImportHolder {}\n");
    try {
      Optional<ASTCompilationUnit> parsed =
          JavaDSLMill.parser().parse_StringCompilationUnit(source.toString());
      if (parsed.isEmpty() || !(parsed.get() instanceof ASTOrdinaryCompilationUnit unit)) {
        throw new IllegalStateException("Could not parse class-diagram imports as Java imports");
      }
      Map<ImportKey, ASTImportDeclaration> result = new LinkedHashMap<>();
      unit.getImportDeclarationList()
          .forEach(importDeclaration -> result.put(ImportKey.from(importDeclaration), importDeclaration));
      if (!result.keySet().equals(importKeys)) {
        throw new IllegalStateException("Java parser changed class-diagram import semantics");
      }
      return result;
    } catch (IOException e) {
      throw new IllegalStateException("Could not parse class-diagram imports as Java imports", e);
    }
  }

  private record ImportKey(String qualifiedName, boolean wildcard) {

    private ImportKey {
      if (qualifiedName == null || qualifiedName.isBlank()) {
        throw new IllegalArgumentException("Import qualified name must not be blank");
      }
    }

    private static ImportKey from(ASTImportDeclaration importDeclaration) {
      return new ImportKey(
          importDeclaration.getMCQualifiedName().getQName(), importDeclaration.isSTAR());
    }

    private String simpleName() {
      return JavaSourceNames.simpleName(qualifiedName);
    }

    private boolean isJavaLang() {
      return wildcard
          ? "java.lang".equals(qualifiedName)
          : qualifiedName.startsWith("java.lang.");
    }
  }
}
