package de.monticore.java;

import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public final class JavaDSLAssertions {

  public static void assertParsingFailure(String pathToModel) {
    JavaDSLParser parser = new JavaDSLParser();

    Optional<ASTCompilationUnit> optCompilationUnit;

    try {
      optCompilationUnit = parser.parse(pathToModel);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    if (!parser.hasErrors() || optCompilationUnit.isPresent()) {
      fail("Successfully parsed invalid model: " + pathToModel);
    }
  }

  public static ASTCompilationUnit assertParsingSuccess(String pathToModel) {
    JavaDSLParser parser = new JavaDSLParser();

    Optional<ASTCompilationUnit> optCompilationUnit;
    Throwable cause = null;

    try {
      optCompilationUnit = parser.parse(pathToModel);
    } catch (IOException e) {
      optCompilationUnit = Optional.empty();
      cause = e;
    }

    if (parser.hasErrors() || optCompilationUnit.isEmpty()) {
      Log.printFindings();
      fail("Failed to parse model: " + pathToModel, cause);
    }

    return optCompilationUnit.get();
  }

}
