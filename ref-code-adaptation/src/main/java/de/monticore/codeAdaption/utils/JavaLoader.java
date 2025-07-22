package de.monticore.codeAdaption.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.monticore.ast.ASTNode;
import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._cocos.CD4CodeCoCoChecker;
import de.monticore.cd4code._parser.CD4CodeParser;
import de.monticore.cd4code._symboltable.CD4CodeSymbolTableCompleter;
import de.monticore.cd4code._symboltable.ICD4CodeArtifactScope;
import de.monticore.cd4code.cocos.CD4CodeCoCosDelegator;
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
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

public class JavaLoader {

  /**
   * Parses a class diagram, builds the symbol table, and checks the consistency conditions (CoCos).
   *
   * @param file The class diagram file to be parsed. It must have a .cd extension.
   * @return The resulting ASTCDCompilationUnit created from the class diagram.
   * @throws AssertionError if the provided file does not have a .cd extension, or if the AST could
   *     not be created successfully.
   */
  public static ASTCDCompilationUnit loadCD(File file) {
    // parse the class diagram
    assert file.getName().endsWith(".cd");
    CD4CodeParser cdParser = new CD4CodeParser();
    Optional<ASTCDCompilationUnit> optCdAST = Optional.empty();
    try {
      optCdAST = cdParser.parse(file.getAbsolutePath());
    } catch (IOException e) {
      System.out.println(e);
      // Log.error("It was not possible to parse the class diagram " + file.getAbsolutePath());
    }
    Assertions.assertTrue(optCdAST.isPresent());

    // create symbol table
    createCDSymTab(optCdAST.get());

    // checkCoCos
    CD4CodeCoCoChecker cdChecker = new CD4CodeCoCosDelegator().getCheckerForAllCoCos();
    //  cdChecker.checkAll(optCdAST.get());
    return optCdAST.get();
  }

  public static ASTCDCompilationUnit parseCD(String cdFile) {
    try {
      Optional<ASTCDCompilationUnit> cd = CD4CodeMill.parser().parseCDCompilationUnit(cdFile);

      if (cd.isPresent()) {
        ICD4CodeArtifactScope as = CD4CodeMill.scopesGenitorDelegator().createFromAST(cd.get());
        as.addImports(new ImportStatement("java.lang.String", true));
        as.addImports(new ImportStatement("java.util", true));
        cd.get().accept(new CD4CodeSymbolTableCompleter(cd.get()).getTraverser());

        return cd.get();

      } else {
        fail("Could not parse CDs.");
      }

    } catch (IOException e) {
      fail(e.getMessage());
    }
    return null;
  }

  private static void createCDSymTab(ASTCDCompilationUnit ast) {
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    ICD4CodeArtifactScope as = CD4CodeMill.scopesGenitorDelegator().createFromAST(ast);
    as.addImports(new ImportStatement("java.lang", true));
    as.addImports(new ImportStatement("java.util", true));
    CD4CodeSymbolTableCompleter c =
        new CD4CodeSymbolTableCompleter(
            ast.getMCImportStatementList(), MCBasicTypesMill.mCQualifiedNameBuilder().build());
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
    assertTrue(javaFile.getName().endsWith(".java"));

    // parse
    JavaDSLTool tool = new JavaDSLTool();
    Optional<ASTCompilationUnit> ast;
    ast = Optional.ofNullable(tool.parse(javaFile.getAbsolutePath()));

    assertTrue(ast.isPresent());
    assertTrue(ast.get() instanceof ASTOrdinaryCompilationUnit);

    // create symbol table
    IJavaDSLGlobalScope globalScope = JavaDSLMill.globalScope();
    JavaDSLScopesGenitor genitor = JavaDSLMill.scopesGenitor();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.setJavaDSLHandler(genitor);
    traverser.add4JavaDSL(genitor);
    genitor.putOnStack(globalScope);

    IJavaDSLArtifactScope artifactScope = genitor.createFromAST(ast.get());
    globalScope.addSubScope(artifactScope);

    return (ASTOrdinaryCompilationUnit) ast.get();
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
      FileUtils.writeStringToFile(path.toFile(), content, Charset.defaultCharset(), false);
    } catch (IOException e) {
      Log.error("Exception occur when writing the file " + path);
    }
  }

  /**
   * Reads recursively all Java files in a directory and its subdirectories.
   *
   * @param directoryPath The root directory to read.
   * @return A set of Java files represented as ASTOrdinaryCompilationUnit.
   */
  public static Set<ASTOrdinaryCompilationUnit> readJavaCode(Path directoryPath) {
    Set<File> res = new HashSet<>();
    readJavaCode(directoryPath, res);
    return res.stream().map(JavaLoader::loadJava).collect(Collectors.toSet());
  }

  /**
   * Reads recursively all Java files in a directory and its subdirectories.
   *
   * @param directoryPath The root directory to read.
   * @return A set of Java files as File objects.
   */
  public static Set<File> readJavaFile(Path directoryPath) {
    Set<File> res = new HashSet<>();
    readJavaCode(directoryPath, res);
    return res;
  }

  private static void readJavaCode(Path directoryPath, Set<File> res) {
    File directory = directoryPath.toFile();
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          readJavaCode(file.toPath(), res);
        } else if (file.isFile() && file.getName().endsWith(".java")) {
          res.add(file);
        }
      }
    }
  }

  public static String readFileContent(File file) {
    byte[] bytes = new byte[0];
    try {
      bytes = Files.readAllBytes(Path.of(file.getAbsolutePath()));
    } catch (IOException e) {
      Log.error("It was not possible to read the file " + file.getAbsolutePath());
    }
    return new String(bytes);
  }

  public static void printAST(Set<ASTOrdinaryCompilationUnit> asts, Path codePath) {
    for (ASTOrdinaryCompilationUnit ast : asts) {
      File sourceFile =
          new File(ast.get_SourcePositionStart().getFileName().orElse(codePath.toString()));
      JavaDSLFullPrettyPrinter prettyPrinter = new JavaDSLFullPrettyPrinter(new IndentPrinter());
      String output = prettyPrinter.prettyprint(ast);
      JavaLoader.writeFile(sourceFile.toPath(), output);
    }
  }

  public static void removeDirectory(Path path) {
    try {
      // Delete the directory and its contents
      FileUtils.deleteDirectory(new File(path.toString()));
      ;
    } catch (IOException e) {
      Log.error("Failed to delete directory: " + e.getMessage());
    }
  }
}
