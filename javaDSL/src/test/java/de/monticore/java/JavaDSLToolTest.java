/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._parser.CD4CodeParser;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTFieldDeclaration;
import de.monticore.java.javadsl._ast.ASTMCQualifiedType;
import de.monticore.java.javadsl._parser.JavaDSLParser;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaDSLToolTest {

  @Test
  public void testTool() {
    JavaDSLTool.main(new String[] {
        "src/test/resources/de/monticore/java/parser/Test.java"
    });
  }

}
