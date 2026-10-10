/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._symboltable.IJavaDSLArtifactScope;
import de.monticore.java.utils.JavaDSLSymbolTableUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;

/**
 * Builds symbol tables for test models. Every build starts with a fresh global scope that only
 * contains the primitive types and resolves JDK types via class2mc. All models of one build are
 * processed together, i.e., they may reference each other, and all symbol table phases are
 * executed (including the type inference of {@code var}).
 *
 * <p>Models given as source code are compiled with javac first, so that the tests can only
 * expect Java semantics of valid Java models, see {@link Javac}. Models that are invalid on
 * purpose or that have already been checked are built with
 * {@link #fromUncheckedSources(String...)}.</p>
 */
public final class SymbolTables {

  private SymbolTables() {
  }

  /** Builds the symbol table of a single valid Java model given as source code. */
  public static IJavaDSLArtifactScope fromSource(String source) {
    return fromSources(source).get(0);
  }

  /** Builds the symbol table of several valid Java models given as source code. */
  public static List<IJavaDSLArtifactScope> fromSources(String... sources) {
    Javac.assertCompiles(sources);
    return fromUncheckedSources(sources);
  }

  /** Builds the symbol table of models without checking them with javac. */
  public static List<IJavaDSLArtifactScope> fromUncheckedSources(String... sources) {
    return fromASTs(Arrays.stream(sources)
        .map(source -> assertParsingSuccess(source, JavaDSLMill.parser()::parse_String))
        .toList());
  }

  /** Builds the symbol table of the given model files. */
  public static List<IJavaDSLArtifactScope> fromFiles(List<Path> files) {
    return fromASTs(files.stream().map(de.monticore.java.JavaDSLAssertions::assertParsingSuccess)
        .toList());
  }

  /** Builds the symbol table of all {@code .java} files in the directory and its subdirectories. */
  public static List<IJavaDSLArtifactScope> fromDirectory(Path directory) {
    return fromFiles(javaFilesIn(directory));
  }

  /** Builds the symbol table of already parsed models. */
  public static List<IJavaDSLArtifactScope> fromASTs(List<ASTCompilationUnit> asts) {
    JavaDSLSymbolTableUtil.prepareMill(true);
    return JavaDSLSymbolTableUtil.buildSymbolTable(asts);
  }

  public static List<Path> javaFilesIn(Path directory) {
    try (Stream<Path> files = Files.walk(directory)) {
      return files.filter(file -> file.toString().endsWith(".java")).sorted().toList();
    }
    catch (IOException e) {
      throw new UncheckedIOException("Cannot list files in " + directory, e);
    }
  }
}
