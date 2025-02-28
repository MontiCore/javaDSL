package de.monticore.codeAdaption.utils;

import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._ast.ASTTypeDeclaration;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import de.monticore.javalight._ast.ASTMethodDeclaration;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavaAstElemCollectorTest extends AdapterAbstractTest {
  private final Path REF_HWC_CODE =
      Path.of("src/test/resources/de/monticore/codeAdaption/utils/ElementCollection.java");

  private JavaAstElemCollector collector;
  private JavaDSLTraverser traverser;

  @BeforeEach
  public void init() {
    initMills();
    traverser = JavaDSLMill.traverser();
    collector = new JavaAstElemCollector();
    traverser.add4JavaDSL(collector);
  }

  @Test
  public void testTypeOperationCollection() {
    ASTOrdinaryCompilationUnit ast = JavaLoader.loadJava(new File(REF_HWC_CODE.toString()));
    ast.accept(traverser);

    // check a type collection
    Assertions.assertEquals(collector.getAllTypeDeclarations().size(), 1);

    // check type element collection
    ASTTypeDeclaration t = collector.getAllTypeDeclarations().get(0);
    Assertions.assertEquals(2, collector.getAllFieldDeclarations(t).size());
    Assertions.assertEquals(1, collector.getAllMethodDeclarations(t).size());

    // check method element collection
    ASTMethodDeclaration m = collector.getAllMethodDeclarations(t).get(0);
    Assertions.assertEquals(2, collector.getAllParameters(t, m).size());
    Assertions.assertEquals(2, collector.getAllLocVariables(t, m).size());
  }
}
