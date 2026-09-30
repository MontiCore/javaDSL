package de.monticore.java._cocos;

import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTJavaDSLNode;
import de.monticore.java.javadsl._ast.ASTLocalVariableDeclaration;
import de.monticore.java.javadsl._cocos.JavaDSLCoCoChecker;
import de.monticore.java.javadsl._cocos.VarUsedWithoutInitializerCoCo;
import de.monticore.runtime.junit.MCAssertions;
import de.monticore.runtime.junit.TestWithMCLanguage;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

@TestWithMCLanguage(JavaDSLMill.class)
public class VarUsedWithoutInitializerCoCoTest {
  @ParameterizedTest
  @ValueSource(strings = {
      "String x",
      "String x = \"Hello World\"",
      "var x = \"Hello World\"",
      "var x = 5",
      "var x = true",
  })
  public void testValidVarStatement(String input) throws IOException {
    ASTLocalVariableDeclaration
        varDec = JavaDSLMill.parser().parse_StringLocalVariableDeclaration(input).orElseGet(
        MCAssertions::failAndPrintFindings);
    
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new VarUsedWithoutInitializerCoCo());
    
    checker.checkAll((ASTJavaDSLNode) varDec);
  }
  
  @ParameterizedTest
  @ValueSource(strings = {
      "var x"
  })
  public void testInvalidVarStatement(String input) throws IOException {
    ASTLocalVariableDeclaration varDec = JavaDSLMill.parser().parse_StringLocalVariableDeclaration(input).orElseGet(
        MCAssertions::failAndPrintFindings);
    
    JavaDSLCoCoChecker checker = new JavaDSLCoCoChecker();
    checker.addCoCo(new VarUsedWithoutInitializerCoCo());
    
    checker.checkAll((ASTJavaDSLNode) varDec);
    
    MCAssertions.assertHasFindingStartingWith(VarUsedWithoutInitializerCoCo.ERROR_CODE);
  }
}
