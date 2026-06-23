package de.monticore.codeAdaption.utils;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTImportDeclaration;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Import cleanup for generated Java sources, backed by JavaDSL source positions. */
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
    for (Path javaFile : javaFiles) {
      String content = Files.readString(javaFile, StandardCharsets.UTF_8);
      SourceState state = removeInvalidImports(content);
      states.put(javaFile, state);
    }

    for (Path javaFile : javaFiles) {
      SourceState state = states.get(javaFile);
      String processed = process(state.content(), javaFile.getFileName().toString());
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
    content = normalizeJavaSource(content);
    Optional<ASTOrdinaryCompilationUnit> ast = parseOrdinaryCompilationUnit(content);
    if (ast.isEmpty()) {
      Log.warn("Could not parse generated Java source for structured import cleanup.");
      return content;
    }

    List<ASTImportDeclaration> invalidImports = knownInvalidImports(ast.get());
    if (invalidImports.isEmpty()) {
      return content;
    }
    return applyImportEdits(content, invalidImports, ast.get());
  }

  private static SourceState removeInvalidImports(String content) {
    content = normalizeJavaSource(content);
    Optional<ASTOrdinaryCompilationUnit> ast = parseOrdinaryCompilationUnit(content);
    if (ast.isEmpty()) {
      Log.warn("Could not parse generated Java source before import cleanup.");
      return new SourceState(content);
    }

    List<ASTImportDeclaration> invalidImports = knownInvalidImports(ast.get());
    String cleaned = applyImportEdits(content, invalidImports, ast.get());
    return new SourceState(cleaned);
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

  private static String normalizeJavaSource(String content) {
    return removeParameterizedImports(content);
  }

  private static String removeParameterizedImports(String content) {
    SourceDocument document = new SourceDocument(content);
    StringBuilder cleaned = new StringBuilder();
    String[] lines = content.split("\\R", -1);
    boolean endsWithLineBreak = content.endsWith("\n") || content.endsWith("\r");
    for (int i = 0; i < lines.length; i++) {
      if (i == lines.length - 1 && lines[i].isEmpty() && endsWithLineBreak) {
        continue;
      }
      String line = lines[i];
      String trimmed = line.trim();
      boolean parameterizedImport =
          trimmed.startsWith("import ")
              && !trimmed.startsWith("import static ")
              && trimmed.endsWith(";")
              && trimmed.substring("import ".length(), trimmed.length() - 1).contains("<");
      if (!parameterizedImport) {
        cleaned.append(line).append(document.lineSeparator());
      }
    }
    if (!endsWithLineBreak && cleaned.length() >= document.lineSeparator().length()) {
      cleaned.setLength(cleaned.length() - document.lineSeparator().length());
    }
    return cleaned.toString();
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

  private static String primaryTypeFileName(String content) {
    return parseOrdinaryCompilationUnit(content)
        .flatMap(JavaSourcePostProcessor::primaryTypeName)
        .map(name -> name + ".java")
        .orElse("Generated.java");
  }

  private static Optional<String> primaryTypeName(ASTOrdinaryCompilationUnit ast) {
    return ast.getTypeDeclarationList().stream().findFirst().map(ASTTypeDeclaration::getName);
  }

  private static String applyImportEdits(
      String content,
      List<ASTImportDeclaration> importsToRemove,
      ASTOrdinaryCompilationUnit ast) {
    if (importsToRemove.isEmpty()) {
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

    return document.apply(edits);
  }

  private record SourceState(String content) {}

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

}
