/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.api.Test;

public class JavaDSLToolTest {

  @Test
  public void testTool() {
    JavaDSLTool.main(new String[] {
        "src/test/resources/de/monticore/java/parser/ASTClassDeclaration.java"
    });
  }

}
