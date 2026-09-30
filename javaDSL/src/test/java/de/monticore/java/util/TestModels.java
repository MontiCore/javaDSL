/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.util;

import java.nio.file.Path;

/**
 * Locations of the test models. The constants are compile-time strings so that they can be used
 * in {@link JavaSourceTest#basePath()}.
 */
public final class TestModels {

  public static final String RESOURCES = "src/test/resources";

  public static final String PARSER = RESOURCES + "/de/monticore/java/parser";

  public static final String PARSABLE_AND_COMPILABLE = RESOURCES + "/parsableAndCompilableModels";

  public static final String SIMPLE_TEST_CLASSES = PARSABLE_AND_COMPILABLE + "/simpleTestClasses";

  public static final String SYMBOL_TABLE = PARSABLE_AND_COMPILABLE + "/symbolTable";

  public static final String UNPARSABLE = RESOURCES + "/unparsableModels";

  /** Sources of the corpus libraries, extracted by the {@code extractCorpus} Gradle task. */
  public static final String CORPUS = "target/corpus";

  private TestModels() {
  }

  /** Resolves a model path relative to {@link #RESOURCES}. */
  public static Path resource(String relativePath) {
    return Path.of(RESOURCES, relativePath);
  }
}
