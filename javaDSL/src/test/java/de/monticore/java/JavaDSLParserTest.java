/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._ast.ASTTextBlockLiteral;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static de.monticore.java.JavaDSLAssertions.*;

import static org.junit.jupiter.api.Assertions.*;

public class JavaDSLParserTest extends AbstractTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "src/test/resources/de/monticore/java/parser/ASTClassDeclaration.java",
      "src/test/resources/de/monticore/java/parser/ParseException.java",
      "src/test/resources/de/monticore/java/parser/TokenMgrError.java",
      "src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java"
  })
  public void testParser(String path) throws IOException {
    assertParsingSuccess(path);
  }

  @Test
  public void testJavaBlock() throws IOException {
    StringBuffer buffer = new StringBuffer();
    buffer.append("{ _channel = HIDDEN;");
    buffer.append("if (getCompiler() != null) {");
    buffer.append("  de.monticore.ast.Comment _comment = new de.monticore.ast.Comment(getText());");
    buffer
        .append("_comment.set_SourcePositionStart(new de.se_rwth.commons.SourcePosition(getLine(), getCharPositionInLine()));");
    buffer.append("_comment.set_SourcePositionEnd(getCompiler().computeEndPosition(getToken()));");
    buffer.append("getCompiler().addComment(_comment);");
    buffer.append("}");
    buffer.append("}   ");
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTJavaBlock> ast = parser.parseJavaBlock(new StringReader(buffer.toString()));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
      "ch = str.charAt(i) < 0x20"
  })
  public void testCondition(String input) throws IOException {
    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTExpression> ast = parser.parse_StringExpression(input);
    assertTrue(ast.isPresent());
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
      "foo -> foo",
      "(foo, bar) -> foo",
      "(foo, bar) -> { return foo; }"
  })
  public void testLambdas(String input) throws IOException {
    JavaDSLParser parser = JavaDSLMill.parser();
    assertTrue(parser.parse_StringExpression(input).isPresent());
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
  public void testMethodReferences(String input) throws IOException {
    JavaDSLParser parser = JavaDSLMill.parser();
    assertTrue(parser.parse_StringExpression(input).isPresent());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "src/test/resources/moduleDeclaration/module-info.java"
  })
  public void testModuleDeclaration(String path) {
    assertParsingSuccess(path);
  }

  @Test
  public void testTextBlocks() throws IOException {
    String textBlock = "\"\"\"" + "\n" +
        "\t\tHello World" + "\n" +
        "\t\t\tIndented" + "\n" +
        "\"\"\"";

    JavaDSLParser parser = JavaDSLMill.parser();
    Optional<ASTLiteral> optLiteral = parser.parse_StringLiteral(textBlock);

    assertTrue(optLiteral.isPresent());

    ASTLiteral literal = optLiteral.get();
    assertInstanceOf(ASTTextBlockLiteral.class, literal);
    assertEquals(
        "Hello World\n\tIndented",
        ((ASTTextBlockLiteral) literal).getSource()
    );
  }

}
