/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.ast.ASTNode;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.PrettyPrinterTester.ParseFunction;
import de.se_rwth.commons.logging.Finding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Assertions for tests of the JavaDSL. All assertions report the current Log-Findings on failure.
 */
public final class JavaDSLAssertions {

  private JavaDSLAssertions() {
  }

  /**
   * Asserts that the model can not be parsed and that the parser reported an error. The reported
   * errors are marked as checked.
   */
  public static void assertParsingFailure(Path pathToModel) {
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTCompilationUnit> optCompilationUnit;

    try {
      optCompilationUnit = parser.parse(pathToModel.toString());
    }
    catch (IOException e) {
      fail("Could not read model: " + pathToModel, e);
      return;
    }

    if (!parser.hasErrors() || optCompilationUnit.isPresent()) {
      MCAssertions.failAndPrintFindings("Successfully parsed invalid model: " + pathToModel);
    }

    MCAssertions.assertHasFindings(Finding::isError);
  }

  /** Asserts that the model can be parsed without errors and returns its AST. */
  public static ASTCompilationUnit assertParsingSuccess(Path pathToModel) {
    return assertParsingSuccess(pathToModel.toString(), JavaDSLMill.parser()::parse);
  }

  /**
   * Asserts that the input can be parsed without errors and returns its AST.
   *
   * <p>Example: {@code assertParsingSuccess("foo -> foo", parser::parse_StringExpression)}</p>
   *
   * @param input     the input, e.g., a file path or the model itself
   * @param parseFunc the parse function of a parser created by {@link JavaDSLMill#parser()}
   */
  public static <N extends ASTNode> N assertParsingSuccess(String input, ParseFunction<N> parseFunc) {
    Optional<N> optAST;

    try {
      optAST = parseFunc.apply(input);
    }
    catch (IOException e) {
      fail("Could not read model: " + input, e);
      return null;
    }

    MCAssertions.assertNoFindings("Failed to parse: " + input);
    return optAST.orElseGet(() -> fail("Parser returned no AST for: " + input));
  }

  /** Asserts that the optional is present and returns its value. */
  public static <T> T assertPresent(Optional<T> optional, String description) {
    assertTrue(optional.isPresent(), "Expected " + description + " to be present");
    return optional.get();
  }
}
