/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTTextBlockLiteral;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.java.util.JavaSourceTest;
import de.monticore.java.util.TestModels;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.runtime.junit.TestWithMCLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;

import static de.monticore.java.JavaDSLAssertions.assertParsingSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@TestWithMCLanguage(JavaDSLMill.class)
public class JavaDSLParserTest {

  private JavaDSLParser parser;

  @BeforeEach
  void createParser() {
    parser = JavaDSLMill.parser();
  }

  @JavaSourceTest(basePath = TestModels.PARSER, files = {
      "ASTClassDeclaration.java", "ParseException.java", "TokenMgrError.java"
  })
  @JavaSourceTest(basePath = TestModels.SIMPLE_TEST_CLASSES, files = {"HelloWorld.java"})
  @JavaSourceTest(basePath = TestModels.RESOURCES + "/moduleDeclaration", files = {"module-info.java"})
  public void testCompilationUnit(Path path) {
    assertParsingSuccess(path);
  }

  @Test
  public void testJavaBlock() {
    String block = """
        { _channel = HIDDEN;
          if (getCompiler() != null) {
            de.monticore.ast.Comment _comment = new de.monticore.ast.Comment(getText());
            _comment.set_SourcePositionStart(new de.se_rwth.commons.SourcePosition(getLine(), getCharPositionInLine()));
            _comment.set_SourcePositionEnd(getCompiler().computeEndPosition(getToken()));
            getCompiler().addComment(_comment);
          }
        }
        """;
    assertParsingSuccess(block, parser::parse_StringMCJavaBlock);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "ch = str.charAt(i) < 0x20"
  })
  public void testCondition(String input) {
    assertParsingSuccess(input, parser::parse_StringExpression);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "foo -> foo",
      "(foo, bar) -> foo",
      "(foo, bar) -> { return foo; }"
  })
  public void testLambdas(String input) {
    assertParsingSuccess(input, parser::parse_StringExpression);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "String::length",
      "System::currentTimeMillis",
      "List<String>::size",
      "int[]::clone",
      "System.out::println",
      "\"abc\"::length",
      "foo[x]::bar",
      "(test ? list.replaceAll(String::trim) : list) :: iterator",
      "super::toString",
      "Arrays::<String>sort"
  })
  public void testMethodReferences(String input) {
    assertParsingSuccess(input, parser::parse_StringExpression);
  }

  @Test
  public void testTextBlocks() {
    String textBlock = "\"\"\"" + "\n" +
        "\t\tHello World" + "\n" +
        "\t\t\tIndented" + "\n" +
        "\"\"\"";

    ASTLiteral literal = assertParsingSuccess(textBlock, parser::parse_StringLiteral);

    ASTTextBlockLiteral textBlockLiteral = assertInstanceOf(ASTTextBlockLiteral.class, literal);
    assertEquals("Hello World\n\tIndented", textBlockLiteral.getSource());
  }

}
