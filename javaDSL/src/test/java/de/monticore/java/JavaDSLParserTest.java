/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.java.javadsl._ast.ASTCompilationUnit;
import de.monticore.java.javadsl._ast.ASTJavaBlock;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class JavaDSLParserTest extends AbstractTest {

  @Test
  public void test1() throws IOException {
    Path model = Paths.get("src/test/resources/de/monticore/java/parser/ASTClassDeclaration.java");
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
//    AST2ModelFiles.get().serializeASTInstance(ast.get(), "ASTClassDeclaration");
  }

  @Test
  public void test2() throws IOException {
    Path model = Paths.get("src/test/resources/de/monticore/java/parser/ParseException.java");
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
 //   AST2ModelFiles.get().serializeASTInstance(ast.get(), "ParseException");
  }

  @Test
  public void test3() throws IOException {
    Path model = Paths.get("src/test/resources/de/monticore/java/parser/TokenMgrError.java");
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    //AST2ModelFiles.get().serializeASTInstance(ast.get(), "TokenMgrError");
  }

  @Test
  public void test4() throws IOException {
    StringBuffer buffer = new StringBuffer("");
    buffer.append("{ _channel = HIDDEN;");
    buffer.append("if (getCompiler() != null) {");
    buffer.append("  de.monticore.ast.Comment _comment = new de.monticore.ast.Comment(getText());");
    buffer
        .append("_comment.set_SourcePositionStart(new de.se_rwth.commons.SourcePosition(getLine(), getCharPositionInLine()));");
    buffer.append("_comment.set_SourcePositionEnd(getCompiler().computeEndPosition(getToken()));");
    buffer.append("getCompiler().addComment(_comment);");
    buffer.append("}");
    buffer.append("}   ");
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTJavaBlock> ast = parser.parseJavaBlock(new StringReader(buffer.toString()));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
  }

  @Test
  public void test5() throws IOException {
    Path model = Paths
        .get("src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java");
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTCompilationUnit> ast = parser.parse(model.toString());
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
//    AST2ModelFiles.get().serializeASTInstance(ast.get(), "ASTClassDeclaration");
  }

  @Test
  public void testCondition() throws IOException {
    JavaDSLParser parser = new JavaDSLParser();
    Optional<ASTExpression> ast = parser.parse_StringExpression("ch = str.charAt(i) < 0x20");
    assertTrue(ast.isPresent());
  }

}
