package de.monticore.codeAdaption.utils;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.SourceVersion;
import spoon.Launcher;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.compiler.VirtualFile;

/** Import cleanup for generated Java sources, backed by JavaDSL and Spoon models. */
public final class JavaSourcePostProcessor {

  private JavaSourcePostProcessor() {}

  public static void processDirectory(Path codePath) throws IOException {
    List<Path> javaFiles;
    try (var paths = Files.walk(codePath)) {
      javaFiles =
          paths
              .filter(Files::isRegularFile)
              .filter(path -> path.toString().endsWith(".java"))
              .sorted()
              .toList();
    }

    Map<Path, SourceState> states = new LinkedHashMap<>();
    Map<String, Set<String>> packageTypes = new LinkedHashMap<>();
    for (Path javaFile : javaFiles) {
      String content = Files.readString(javaFile, StandardCharsets.UTF_8);
      SourceState state = removeInvalidImports(content);
      states.put(javaFile, state);
      packageTypes
          .computeIfAbsent(state.packageName(), ignored -> new LinkedHashSet<>())
          .addAll(state.declaredTypes());
    }

    for (Path javaFile : javaFiles) {
      SourceState state = states.get(javaFile);
      Set<String> samePackageTypes = packageTypes.getOrDefault(state.packageName(), Set.of());
      String processed =
          process(state.content(), javaFile.getFileName().toString(), samePackageTypes);
      String original = Files.readString(javaFile, StandardCharsets.UTF_8);
      if (!processed.equals(original)) {
        Files.writeString(javaFile, processed, StandardCharsets.UTF_8);
      }
    }
  }

  public static String process(String content) {
    return process(content, primaryTypeFileName(content));
  }

  public static String process(String content, String fileName) {
    return process(content, fileName, Set.of());
  }

  private static String process(String content, String fileName, Set<String> samePackageTypes) {
    Optional<ASTOrdinaryCompilationUnit> ast = parseOrdinaryCompilationUnit(content);
    if (ast.isEmpty()) {
      Log.warn("Could not parse generated Java source for structured import cleanup.");
      return content;
    }

    List<ASTImportDeclaration> invalidImports = knownInvalidImports(ast.get());
    removeKnownInvalidImports(ast.get());
    String sourceForSpoon = applyImportEdits(content, invalidImports, Set.of(), ast.get());
    String effectiveFileName = effectiveFileName(fileName, ast.get());
    Set<String> missingJavaUtilImports =
        missingJavaUtilImports(sourceForSpoon, effectiveFileName, ast.get(), samePackageTypes);
    if (invalidImports.isEmpty() && missingJavaUtilImports.isEmpty()) {
      return content;
    }
    return applyImportEdits(content, invalidImports, missingJavaUtilImports, ast.get());
  }

  private static SourceState removeInvalidImports(String content) {
    Optional<ASTOrdinaryCompilationUnit> ast = parseOrdinaryCompilationUnit(content);
    if (ast.isEmpty()) {
      Log.warn("Could not parse generated Java source before import cleanup.");
      return new SourceState(content, "", Set.of());
    }

    List<ASTImportDeclaration> invalidImports = knownInvalidImports(ast.get());
    removeKnownInvalidImports(ast.get());
    String cleaned = applyImportEdits(content, invalidImports, Set.of(), ast.get());
    return new SourceState(cleaned, packageName(ast.get()), declaredTypeNames(ast.get()));
  }

  private static Optional<ASTOrdinaryCompilationUnit> parseOrdinaryCompilationUnit(
      String content) {
    try {
      Optional<ASTCompilationUnit> ast = JavaDSLMill.parser().parse_StringCompilationUnit(content);
      return ast.filter(ASTOrdinaryCompilationUnit.class::isInstance)
          .map(ASTOrdinaryCompilationUnit.class::cast);
    } catch (IOException | RuntimeException e) {
      Log.debug(
          "JavaDSL could not parse generated source: " + e.getMessage(),
          "JavaSourcePostProcessor");
      return Optional.empty();
    }
  }

  private static boolean removeKnownInvalidImports(ASTOrdinaryCompilationUnit ast) {
    List<ASTImportDeclaration> validImports =
        ast.getImportDeclarationList().stream()
            .filter(importDeclaration -> !isKnownInvalidImport(importDeclaration))
            .toList();
    if (validImports.size() == ast.getImportDeclarationList().size()) {
      return false;
    }
    ast.setImportDeclarationList(new ArrayList<>(validImports));
    return true;
  }

  private static List<ASTImportDeclaration> knownInvalidImports(ASTOrdinaryCompilationUnit ast) {
    return ast.getImportDeclarationList().stream()
        .filter(JavaSourcePostProcessor::isKnownInvalidImport)
        .toList();
  }

  private static boolean isKnownInvalidImport(ASTImportDeclaration importDeclaration) {
    if (importDeclaration.isStatic()) {
      return false;
    }
    String imported = importDeclaration.getMCQualifiedName().getQName();
    return Constants.ANNOT_PACKAGE.equals(imported) || !imported.contains(".");
  }

  private static Set<String> missingJavaUtilImports(
      String content,
      String fileName,
      ASTOrdinaryCompilationUnit ast,
      Set<String> samePackageTypes) {
    ImportModel imports = ImportModel.from(ast);
    if (imports.hasJavaUtilWildcard()) {
      return Set.of();
    }

    Set<String> localTypeNames = new LinkedHashSet<>(declaredTypeNames(ast));
    localTypeNames.addAll(samePackageTypes);
    Set<String> used = usedJavaUtilSimpleNames(content, fileName, localTypeNames);
    if (used.isEmpty()) {
      return Set.of();
    }

    Set<String> missing = new LinkedHashSet<>();
    for (String simpleName : used) {
      if (!imports.importsSimpleName(simpleName) && !localTypeNames.contains(simpleName)) {
        missing.add("java.util." + simpleName);
      }
    }
    return missing;
  }

  private static Set<String> usedJavaUtilSimpleNames(
      String content, String fileName, Set<String> localTypeNames) {
    try {
      Launcher launcher = new Launcher();
      launcher.getEnvironment().setNoClasspath(true);
      launcher.addInputResource(new VirtualFile(content, fileName));
      launcher.buildModel();

      Set<String> declaredTypes = new LinkedHashSet<>(localTypeNames);
      for (CtType<?> type : launcher.getModel().getAllTypes()) {
        declaredTypes.add(type.getSimpleName());
      }

      Set<String> result = new TreeSet<>();
      List<CtTypeReference<?>> references =
          launcher.getModel().getElements(new TypeFilter<>(CtTypeReference.class));
      for (CtTypeReference<?> reference : references) {
        javaUtilCandidate(reference)
            .filter(simpleName -> !declaredTypes.contains(simpleName))
            .filter(JavaSourcePostProcessor::isPublicJavaUtilType)
            .ifPresent(result::add);
      }
      return new LinkedHashSet<>(result);
    } catch (Exception e) {
      Log.debug(
          "Spoon could not inspect generated source: " + e.getMessage(),
          "JavaSourcePostProcessor");
      return Set.of();
    }
  }

  private static Optional<String> javaUtilCandidate(CtTypeReference<?> reference) {
    CtTypeReference<?> elementReference = elementReference(reference);
    String simpleName = elementReference.getSimpleName();
    if (simpleName == null
        || simpleName.isBlank()
        || !SourceVersion.isIdentifier(simpleName)
        || SourceVersion.isKeyword(simpleName)) {
      return Optional.empty();
    }

    String qualifiedName = elementReference.getQualifiedName();
    if (!elementReference.isSimplyQualified()
        && qualifiedName != null
        && qualifiedName.contains(".")
        && !qualifiedName.equals(simpleName)) {
      return Optional.empty();
    }
    return Optional.of(simpleName);
  }

  private static CtTypeReference<?> elementReference(CtTypeReference<?> reference) {
    CtTypeReference<?> current = reference;
    while (current instanceof CtArrayTypeReference<?> arrayReference) {
      current = arrayReference.getComponentType();
    }
    return current;
  }

  private static boolean isPublicJavaUtilType(String simpleName) {
    try {
      Class<?> type =
          Class.forName("java.util." + simpleName, false, ClassLoader.getPlatformClassLoader());
      return "java.util".equals(type.getPackageName())
          && type.getEnclosingClass() == null
          && Modifier.isPublic(type.getModifiers());
    } catch (ClassNotFoundException ignored) {
      return false;
    }
  }

  private static String primaryTypeFileName(String content) {
    return parseOrdinaryCompilationUnit(content)
        .flatMap(JavaSourcePostProcessor::primaryTypeName)
        .map(name -> name + ".java")
        .orElse("Generated.java");
  }

  private static String effectiveFileName(String fileName, ASTOrdinaryCompilationUnit ast) {
    if (fileName == null || fileName.isBlank()) {
      return primaryTypeName(ast).map(name -> name + ".java").orElse("Generated.java");
    }
    try {
      String simpleFileName = Path.of(fileName).getFileName().toString();
      return simpleFileName.endsWith(".java") ? simpleFileName : simpleFileName + ".java";
    } catch (InvalidPathException ignored) {
      return fileName.endsWith(".java") ? fileName : fileName + ".java";
    }
  }

  private static Optional<String> primaryTypeName(ASTOrdinaryCompilationUnit ast) {
    return ast.getTypeDeclarationList().stream().findFirst().map(ASTTypeDeclaration::getName);
  }

  private static String packageName(ASTOrdinaryCompilationUnit ast) {
    return ast.isPresentPackageDeclaration()
        ? ast.getPackageDeclaration().getMCQualifiedName().getQName()
        : "";
  }

  private static Set<String> declaredTypeNames(ASTOrdinaryCompilationUnit ast) {
    Set<String> typeNames = new LinkedHashSet<>();
    ast.getTypeDeclarationList().stream().map(ASTTypeDeclaration::getName).forEach(typeNames::add);
    return typeNames;
  }

  private static String applyImportEdits(
      String content,
      List<ASTImportDeclaration> importsToRemove,
      Set<String> importsToAdd,
      ASTOrdinaryCompilationUnit ast) {
    if (importsToRemove.isEmpty() && importsToAdd.isEmpty()) {
      return content;
    }

    SourceDocument document = new SourceDocument(content);
    List<SourceEdit> edits = new ArrayList<>();
    for (ASTImportDeclaration importToRemove : importsToRemove) {
      int line = importToRemove.get_SourcePositionStart().getLine();
      if (line > 0) {
        edits.add(new SourceEdit(document.lineStart(line), document.lineEndIncludingEnding(line), ""));
      }
    }

    if (!importsToAdd.isEmpty()) {
      int insertionOffset = importInsertionOffset(document, ast);
      StringBuilder insertion = new StringBuilder();
      if (ast.getImportDeclarationList().isEmpty() && ast.isPresentPackageDeclaration()) {
        insertion.append(document.lineSeparator());
      }
      for (String importName : importsToAdd) {
        insertion.append("import ").append(importName).append(";").append(document.lineSeparator());
      }
      edits.add(new SourceEdit(insertionOffset, insertionOffset, insertion.toString()));
    }

    return document.apply(edits);
  }

  private static int importInsertionOffset(SourceDocument document, ASTOrdinaryCompilationUnit ast) {
    return ast.getImportDeclarationList().stream()
        .map(ASTImportDeclaration::get_SourcePositionStart)
        .max(SourcePosition::compareTo)
        .map(position -> document.lineEndIncludingEnding(position.getLine()))
        .orElseGet(
            () ->
                ast.isPresentPackageDeclaration()
                    ? document.lineEndIncludingEnding(
                        ast.getPackageDeclaration().get_SourcePositionStart().getLine())
                    : 0);
  }

  private record SourceState(String content, String packageName, Set<String> declaredTypes) {}

  private record SourceEdit(int start, int end, String replacement) {}

  private static final class SourceDocument {
    private final String content;
    private final List<Integer> lineStarts;
    private final String lineSeparator;

    private SourceDocument(String content) {
      this.content = content;
      this.lineStarts = lineStarts(content);
      this.lineSeparator = detectLineSeparator(content);
    }

    private int lineStart(int line) {
      if (line <= 1) {
        return 0;
      }
      if (line > lineStarts.size()) {
        return content.length();
      }
      return lineStarts.get(line - 1);
    }

    private int lineEndIncludingEnding(int line) {
      if (line < lineStarts.size()) {
        return lineStarts.get(line);
      }
      return content.length();
    }

    private String lineSeparator() {
      return lineSeparator;
    }

    private String apply(List<SourceEdit> edits) {
      StringBuilder result = new StringBuilder(content);
      edits.stream()
          .sorted(
              Comparator.comparingInt(SourceEdit::start)
                  .thenComparingInt(SourceEdit::end)
                  .reversed())
          .forEach(edit -> result.replace(edit.start(), edit.end(), edit.replacement()));
      return result.toString();
    }

    private static List<Integer> lineStarts(String content) {
      List<Integer> starts = new ArrayList<>();
      starts.add(0);
      for (int i = 0; i < content.length(); i++) {
        if (content.charAt(i) == '\n') {
          starts.add(i + 1);
        }
      }
      return starts;
    }

    private static String detectLineSeparator(String content) {
      int newline = content.indexOf('\n');
      if (newline > 0 && content.charAt(newline - 1) == '\r') {
        return "\r\n";
      }
      return "\n";
    }
  }

  private record ImportModel(Set<String> imported, boolean hasJavaUtilWildcard) {
    private static ImportModel from(ASTOrdinaryCompilationUnit ast) {
      Set<String> imported = new LinkedHashSet<>();
      boolean wildcard = false;
      for (ASTImportDeclaration importDeclaration : ast.getImportDeclarationList()) {
        if (importDeclaration.isStatic()) {
          continue;
        }
        String importName = importDeclaration.getMCQualifiedName().getQName();
        if (importDeclaration.isSTAR() && "java.util".equals(importName)) {
          wildcard = true;
        }
        imported.add(importDeclaration.isSTAR() ? importName + ".*" : importName);
      }
      return new ImportModel(imported, wildcard);
    }

    private boolean importsSimpleName(String simpleName) {
      return imported.stream().anyMatch(importName -> importName.endsWith("." + simpleName));
    }
  }
}
