package de.monticore.codeAdaption.matcher;

import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.codeAdaption.AdapterAbstractTest;
import de.monticore.codeAdaption.utils.JavaLoader;
import de.monticore.codeAdaption.utils.visitors.JavaAstElemCollector;
import de.monticore.java.javadsl.JavaDSLMill;
import de.monticore.java.javadsl._ast.ASTOrdinaryCompilationUnit;
import de.monticore.java.javadsl._visitor.JavaDSLTraverser;
import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;

public abstract class MatcherAbstractTest extends AdapterAbstractTest {
  protected final Path REF_HWC_Dir =
      Path.of("src/test/resources/de/monticore/codeAdaption/matcher");
  protected final Path REF_CD = Path.of("src/test/resources/de/monticore/codeAdaption/App.cd");

  protected ASTCDCompilationUnit cd;

  protected JavaAstElemCollector collector;

  @BeforeEach
  public void setup() {
    initMills();
  }

  public void init(String javaFile) {
    collector = new JavaAstElemCollector();
    JavaDSLTraverser traverser = JavaDSLMill.traverser();
    traverser.add4JavaDSL(collector);
    ASTOrdinaryCompilationUnit ast = JavaLoader.loadJava(new File(REF_HWC_Dir + javaFile));
    cd = JavaLoader.loadCD(new File(REF_CD.toString()));

    ast.accept(traverser);
  }
}
