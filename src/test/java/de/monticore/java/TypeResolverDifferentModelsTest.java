/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import de.se_rwth.commons.logging.LogStub;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.java.types.JavaDSLTypeChecker;
import de.se_rwth.commons.logging.Log;

/**
 *  on 19.08.2016.
 */
public class TypeResolverDifferentModelsTest extends AbstractCoCoTestClass {

  JavaDSLTypeChecker typeChecker = new JavaDSLTypeChecker();

  @BeforeClass
  public static void init() {
    LogStub.init();
    LogStub.enableFailQuick(false);
  }

  @Before
  public void setUp() {
    LogStub.getFindings().clear();
  }

  /*
   * Tests for annotation CoCos
   */

  @Test
  public void testSymbolReference(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/SymbolReference", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testSymbol(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/Symbol", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testTypeSymbol(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/TypeSymbol", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testSymbolKind(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/SymbolKind", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testTypeSymbolKind(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/TypeSymbolKind", typeChecker.getAllTypeChecker());
  }


  @Test
  public void testAmbiguityException(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/AmbiguityException", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testAstAdditionalAttributes(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/AstAdditionalAttributes", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testEmfAttribute(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/EmfAttribute", typeChecker.getAllTypeChecker());
  }


  @Test
  public void testLiteralsHelper(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/LiteralsHelper", typeChecker.getAllTypeChecker());
  }

  @Test
  public void testMC2CDStereotypes(){
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/valid/MC2CDStereotypes", typeChecker.getAllTypeChecker());
  }
  
  @Test
  public void testHelloWorld(){
    testModelNoErrors("src/test/resources",
        "parsableAndCompilableModels/simpleTestClasses/HelloWorld", typeChecker.getAllTypeChecker());
  }

}
