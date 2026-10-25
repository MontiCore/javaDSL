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
    StringBuilder cleaned = new StringBuilder(content.length());
    LexicalState state = LexicalState.NORMAL;
    int lineStart = 0;
    while (lineStart < content.length()) {
      int lineEnd = lineStart;
      while (lineEnd < content.length()
          && content.charAt(lineEnd) != '\r'
          && content.charAt(lineEnd) != '\n') {
        lineEnd++;
      }
      int nextLine = lineEnd;
      if (nextLine < content.length() && content.charAt(nextLine) == '\r') {
        nextLine++;
      }
      if (nextLine < content.length() && content.charAt(nextLine) == '\n') {
        nextLine++;
      }

      String line = content.substring(lineStart, lineEnd);
      if (!(state == LexicalState.NORMAL && isParameterizedImportLine(line))) {
        cleaned.append(content, lineStart, nextLine);
      }
      state = advanceLexicalState(content, lineStart, nextLine, state);
      lineStart = nextLine;
    }
    return cleaned.toString();
  }

  private static boolean isParameterizedImportLine(String line) {
    String trimmed = line.trim();
    return trimmed.startsWith("import ")
        && !trimmed.startsWith("import static ")
        && trimmed.endsWith(";")
        && trimmed.substring("import ".length(), trimmed.length() - 1).contains("<");
  }

  private static LexicalState advanceLexicalState(
      String content, int start, int end, LexicalState initialState) {
    LexicalState state = initialState;
    for (int index = start; index < end; index++) {
      char current = content.charAt(index);
      char next = index + 1 < end ? content.charAt(index + 1) : '\0';
      switch (state) {
        case NORMAL -> {
          if (current == '/' && next == '/') {
            state = LexicalState.LINE_COMMENT;
            index++;
          } else if (current == '/' && next == '*') {
            state = LexicalState.BLOCK_COMMENT;
            index++;
          } else if (current == '"'
              && index + 2 < end
              && content.charAt(index + 1) == '"'
              && content.charAt(index + 2) == '"') {
            state = LexicalState.TEXT_BLOCK;
            index += 2;
          } else if (current == '"') {
            state = LexicalState.STRING;
          } else if (current == '\'') {
            state = LexicalState.CHARACTER;
          }
        }
        case LINE_COMMENT -> {
          if (current == '\r' || current == '\n') {
            state = LexicalState.NORMAL;
          }
        }
        case BLOCK_COMMENT -> {
          if (current == '*' && next == '/') {
            state = LexicalState.NORMAL;
            index++;
          }
        }
        case STRING -> {
          if (current == '\\') {
            index++;
          } else if (current == '"' || current == '\r' || current == '\n') {
            state = LexicalState.NORMAL;
          }
        }
        case CHARACTER -> {
          if (current == '\\') {
            index++;
          } else if (current == '\'' || current == '\r' || current == '\n') {
            state = LexicalState.NORMAL;
          }
        }
        case TEXT_BLOCK -> {
          if (current == '"'
              && index + 2 < end
              && content.charAt(index + 1) == '"'
              && content.charAt(index + 2) == '"'
              && !isEscaped(content, index)) {
            state = LexicalState.NORMAL;
            index += 2;
          }
        }
      }
    }
    return state;
  }

  private static boolean isEscaped(String content, int index) {
    int backslashes = 0;
    for (int cursor = index - 1; cursor >= 0 && content.charAt(cursor) == '\\'; cursor--) {
      backslashes++;
    }
    return backslashes % 2 != 0;
  }

  private enum LexicalState {
    NORMAL,
    LINE_COMMENT,
    BLOCK_COMMENT,
    STRING,
    CHARACTER,
    TEXT_BLOCK
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
