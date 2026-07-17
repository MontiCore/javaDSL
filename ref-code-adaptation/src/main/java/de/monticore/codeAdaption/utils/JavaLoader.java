package de.monticore.codeAdaption.utils;

import de.monticore.ast.ASTNode;
import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._parser.CD4CodeParser;
import de.monticore.cd4code._symboltable.CD4CodeSymbolTableCompleter;
import de.monticore.cd4code._symboltable.ICD4CodeArtifactScope;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl.JavaDSLTool;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTMCBasicGenericType;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._prettyprint.JavaDSLFullPrettyPrinter;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.javadsl._symboltable.IJavaDSLGlobalScope;
import de.monticore.java.javadsl._symboltable.JavaDSLScopesGenitor;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;

/**
 * File-system and parser utilities for the JavaDSL and CD4Code models used during adaptation.
 *
 * <p>Loading creates the symbol tables required by the matching and update phases. Semantic
 * validation is deliberately owned by the caller: this utility does not select or execute a
 * CD4Code CoCo set because different adaptation inputs permit different degrees of
 * underspecification.
 */
public class JavaLoader {

  /**
   * Parses a class diagram and builds the symbol table required for name and type resolution.
   *
   * <p>No CD4Code CoCos are executed. Callers that require a particular semantic profile must run
   * its checker explicitly after loading.
   *
   * @param file The class diagram file to be parsed. It must have a .cd extension.
   * @return The resulting ASTCDCompilationUnit created from the class diagram.
   * @throws IllegalArgumentException if the provided file is not a readable CD file
   * @throws IllegalStateException if parsing fails
   */
  public static ASTCDCompilationUnit loadCD(File file) {
    // parse the class diagram
    requireFileExtension(file, ".cd");
    CD4CodeParser cdParser = new CD4CodeParser();
    Optional<ASTCDCompilationUnit> optCdAST = Optional.empty();
    try {
      optCdAST = cdParser.parse(file.getAbsolutePath());
    } catch (IOException e) {
      throw new IllegalStateException("Could not read class diagram " + file.getAbsolutePath(), e);
    }
    if (optCdAST.isEmpty()) {
      throw new IllegalStateException("Could not parse class diagram " + file.getAbsolutePath());
    }

    // create symbol table
    initializeCDSymbolTable(optCdAST.get());

    return optCdAST.get();
  }

  /**
   * Parses a CD file with the same symbol-table setup used by {@link #loadCD(File)}, but without
   * requiring the diagram name to match the file name. This is used by adaptation fixtures whose
   * file names are scenario-oriented.
   */
  public static ASTCDCompilationUnit parseCD(String cdFile) {
    try {
      Optional<ASTCDCompilationUnit> cd = CD4CodeMill.parser().parseCDCompilationUnit(cdFile);

      if (cd.isPresent()) {
        initializeCDSymbolTable(cd.get());
        return cd.get();

      }
      throw new IllegalStateException("Could not parse class diagram " + cdFile);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read class diagram " + cdFile, e);
    }
  }

  /** Rebuilds the symbol table for a parsed or cloned class-diagram AST. */
  public static void initializeCDSymbolTable(ASTCDCompilationUnit ast) {
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    ICD4CodeArtifactScope as = CD4CodeMill.scopesGenitorDelegator().createFromAST(ast);
    CD4CodeSymbolTableCompleter c = new CD4CodeSymbolTableCompleter(ast);
    ast.accept(c.getTraverser());
    ast.setEnclosingScope(as);
  }

  /**
   * Loads a Java file, transforms it to an AST, and creates symbol tables.
   *
   * @param javaFile The Java file to be loaded and processed.
   * @return The resulting ASTOrdinaryCompilationUnit created from the Java file.
   */
  public static ASTOrdinaryCompilationUnit loadJava(File javaFile) {
    requireFileExtension(javaFile, ".java");

    // parse
    JavaDSLTool tool = new JavaDSLTool();
    Optional<ASTCompilationUnit> ast;
    ast = Optional.ofNullable(tool.parse(javaFile.getAbsolutePath()));

    if (ast.isEmpty()) {
      throw new IllegalStateException("Could not parse Java source " + javaFile.getAbsolutePath());
    }
    if (!(ast.get() instanceof ASTOrdinaryCompilationUnit)) {
      throw new IllegalStateException(
          "Expected an ordinary Java compilation unit in " + javaFile.getAbsolutePath());
    }
    ASTOrdinaryCompilationUnit ordinaryCompilationUnit = (ASTOrdinaryCompilationUnit) ast.get();

    // create symbol table
    IJavaDSLGlobalScope globalScope = JavaDSLMill.globalScope();
    JavaDSLScopesGenitor genitor = JavaDSLMill.scopesGenitor();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.setJavaDSLHandler(genitor);
    traverser.add4JavaDSL(genitor);
    genitor.putOnStack(globalScope);

    IJavaDSLArtifactScope artifactScope = genitor.createFromAST(ordinaryCompilationUnit);
    globalScope.addSubScope(artifactScope);

    return ordinaryCompilationUnit;
  }

  /**
   * Prints a Java ASTNode as a formatted string.
   *
   * @param node The ASTNode to be printed.
   * @return A formatted string representation of the provided ASTNode.
   */
  public static String print(ASTNode node) {
    JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
    return prettyPrinter.prettyprint(node);
  }

  public static String print(ASTMCType type) {
    if (type instanceof ASTMCBasicGenericType) {
      return ((ASTMCBasicGenericType) type).getAnnotatedName(0).getName();
    }
    return print((ASTNode) type);
  }

  public static void writeFile(Path path, String content) {
    try {
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      Files.writeString(path, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Could not write file " + path, e);
    }
  }

  /**
   * Reads recursively all Java files in a directory and its subdirectories.
   *
   * @param directoryPath The root directory to read.
   * @return A set of Java files represented as ASTOrdinaryCompilationUnit.
  */
  public static Set<ASTOrdinaryCompilationUnit> readJavaCode(Path directoryPath) {
    // Clear stale artifact scopes before loading a new source batch.
    JavaDSLMill.globalScope().clear();
    Set<File> res = new LinkedHashSet<>();
    readJavaCode(directoryPath, res);
    return res.stream().map(JavaLoader::loadJava).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  /**
   * Reads recursively all Java files in a directory and its subdirectories.
   *
   * @param directoryPath The root directory to read.
   * @return A set of Java files as File objects.
   */
  public static Set<File> readJavaFile(Path directoryPath) {
    Set<File> res = new LinkedHashSet<>();
    readJavaCode(directoryPath, res);
    return res;
  }

  private static void readJavaCode(Path directoryPath, Set<File> res) {
    Path normalized = directoryPath.toAbsolutePath().normalize();
    if (!Files.exists(normalized)) {
      throw new IllegalArgumentException("Java source directory does not exist: " + normalized);
    }
    if (!Files.isDirectory(normalized)) {
      throw new IllegalArgumentException("Java source path is not a directory: " + normalized);
    }
    try (var paths = Files.walk(normalized)) {
      paths.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".java"))
          .sorted(Comparator.comparing(Path::toString))
          .map(Path::toFile)
          .forEach(res::add);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read Java source directory " + normalized, e);
    }
  }

  public static String readFileContent(File file) {
    try {
      return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Could not read file " + file.getAbsolutePath(), e);
    }
  }

  /**
   * Pretty-prints Java ASTs into {@code codePath}, preserving package directories when present.
   */
  public static void printAST(Set<ASTOrdinaryCompilationUnit> asts, Path codePath) {
    for (ASTOrdinaryCompilationUnit ast : asts) {
      Path targetPath = outputPathFor(ast, codePath);

      JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
      String output = prettyPrinter.prettyprint(ast);
      JavaLoader.writeFile(targetPath, output);
    }
  }

  private static Path outputPathFor(ASTOrdinaryCompilationUnit ast, Path codePath) {
    String fileName = primaryTypeFileName(ast).orElseGet(() -> sourceFileName(ast).orElse("Unknown.java"));
    if (ast.isPresentPackageDeclaration()) {
      Path packagePath =
          Path.of(
              ast.getPackageDeclaration()
                  .getMCQualifiedName()
                  .getQName()
                  .replace('.', File.separatorChar));
      return codePath.resolve(packagePath).resolve(fileName);
    }
    return codePath.resolve(sourceFileName(ast).orElse(fileName));
  }

  private static Optional<String> primaryTypeFileName(ASTOrdinaryCompilationUnit ast) {
    return ast.getTypeDeclarationList().stream()
        .findFirst()
        .map(type -> type.getName() + ".java");
  }

  private static Optional<String> sourceFileName(ASTOrdinaryCompilationUnit ast) {
    return ast.get_SourcePositionStart()
        .getFileName()
        .flatMap(
            fileName -> {
              try {
                return Optional.of(Path.of(fileName).getFileName().toString());
              } catch (InvalidPathException e) {
                return Optional.empty();
              }
            });
  }

  /**
   * Deletes an output directory before adaptation writes new generated code.
   */
  public static void removeDirectory(Path path) {
    Path normalized = path.toAbsolutePath().normalize();
    if (normalized.getParent() == null) {
      throw new IllegalArgumentException("Refusing to delete filesystem root: " + normalized);
    }
    try {
      FileUtils.deleteDirectory(normalized.toFile());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to delete directory " + normalized, e);
    }
  }

  private static void requireFileExtension(File file, String extension) {
    Objects.requireNonNull(file, "file");
    if (!file.getName().endsWith(extension)) {
      throw new IllegalArgumentException("Expected a " + extension + " file: " + file);
    }
    if (!file.isFile()) {
      throw new IllegalArgumentException("File does not exist or is not a regular file: " + file);
    }
  }
}
