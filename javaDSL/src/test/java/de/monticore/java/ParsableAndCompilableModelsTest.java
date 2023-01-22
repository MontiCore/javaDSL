/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.jupiter.api.Test;

<<<<<<< HEAD
/**
 * A simple test for java tool.
 *
 */
public class ParsableAndCompilableModelsTest extends AbstractTestClass {
=======
import static de.monticore.java.JavaDSLAssertions.*;

public class ParsableAndCompilableModelsTest extends AbstractTest {
>>>>>>> b1632b992b7e88612c226e7c337d14e8cbff6536

  // package parserBugs.*
  @Test
  public void test_parserBugs_ExplicitGenericInvocations() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/parserBugs/ExplicitGenericInvocations.java");
  }

  @Test
  public void test_parserBugs_InterfaceWithStaticMethod() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/parserBugs/InterfaceWithStaticMethod.java");
  }

  // package basicTestClasses.types.*
  @Test
  public void test_simpleTestClasses_types_AnnotationTestModel() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleAnnotationTestModel.java");
  }

  @Test
  public void test_simpleTestClasses_types_SimpleClassTestModel() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleClassTestModel.java");
  }

  @Test
  public void test_simpleTestClasses_types_SimpleEnumTestModels() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleEnumTestModels.java");
  }

  @Test
  public void test_simpleTestClasses_types_SimpleInterfaceTestModel() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/types/SimpleInterfaceTestModel.java");
  }

  // package simpleTestClasses.*
  @Test
  public void test_simpleTestClasses_EmptyClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/EmptyClass.java");
  }

  @Test
  public void test_simpleTestClasses_HelloWorld() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/HelloWorld.java");
  }

  @Test
  public void test_simpleTestClasses_OneFieldClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/OneFieldClass.java");
  }

  @Test
  public void test_simpleTestClasses_QualifiedNameTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/simpleTestClasses/QualifiedNameTestClass.java");
  }

  // package stressfulPackage
  @Test
  public void test_stressfulPackage_StressfulSyntax() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/stressfulPackage/StressfulSyntax.java");
  }

  // package symbolTable.enums.*
  @Test
  public void test_symbolTable_enums_EnumViaJavaEnum() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaEnum.java");
  }

  @Test
  public void test_symbolTable_enums_EnumViaJavaInterface() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/enums/EnumViaJavaInterface.java");
  }

  // package symbolTable.resolve.*
  @Test
  public void test_symbolTable_resolve_GeneralResolveTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/resolve/GeneralResolveTestClass.java");
  }

  @Test
  public void test_symbolTable_resolve_TypeVariableShadowingTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/resolve/TypeVariableShadowingTestClass.java");
  }

  // package symbolTable.typeArgumentsAndParameters.*
  @Test
  public void test_TypeArgumentsAndParameters_TypeArgumentTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass.java");
  }

  @Test
  public void test_TypeArgumentsAndParameters_TypeParameterTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/typeArgumentsAndParameters/TypeParameterTestClass.java");
  }

  // package symbolTable.*
  @Test
  public void test_symbolTable_ScopesSymbolTableTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/ScopesSymbolTableTestClass.java");
  }

  @Test
  public void test_BasicTestClasses_VariablesTestClass() {
    assertParsingSuccess("src/test/resources/parsableAndCompilableModels/symbolTable/VariablesTestClass.java");
  }

}
