/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java;

import org.junit.Ignore;
import org.junit.Test;

/**
 * A simple test for java tool.
 *
 */
public class ParsableAndCompilableModelsTest extends AbstractTestClass {

  // package parserBugs.*
  @Test
  public void test_parserBugs_ExplicitGenericInvocations() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "parserBugs/ExplicitGenericInvocations");
  }

  @Test
  public void test_parserBugs_InterfaceWithStaticMethod() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "parserBugs/InterfaceWithStaticMethod");
  }

  // package basicTestClasses.types.*
  @Test
  public void test_simpleTestClasses_types_AnnotationTestModel() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleAnnotationTestModel");
  }

  @Test
  public void test_simpleTestClasses_types_SimpleClassTestModel() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleClassTestModel");
  }

  @Test
  public void test_simpleTestClasses_types_SimpleEnumTestModels() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleEnumTestModels");
  }

  @Test
  public void test_simpleTestClasses_types_SimpleInterfaceTestModel() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/types/SimpleInterfaceTestModel");
  }

  // package simpleTestClasses.*
  @Test
  public void test_simpleTestClasses_EmptyClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/EmptyClass");
  }

  @Test
  public void test_simpleTestClasses_HelloWorld() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/HelloWorld");
  }

  @Test
  public void test_simpleTestClasses_OneFieldClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/OneFieldClass");
  }

  @Test
  public void test_simpleTestClasses_QualifiedNameTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "simpleTestClasses/QualifiedNameTestClass");
  }

  // package stressfulPackage
  @Test
  @Ignore
  public void test_stressfulPackage_StressfulSyntax() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "stressfulPackage/StressfulSyntax");
  }

  // package symbolTable.enums.*
  @Test
  public void test_symbolTable_enums_EnumViaJavaEnum() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/enums/EnumViaJavaEnum");
  }

  @Test
  public void test_symbolTable_enums_EnumViaJavaInterface() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/enums/EnumViaJavaInterface");
  }

  // package symbolTable.resolve.*
  @Test
  public void test_symbolTable_resolve_GeneralResolveTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/resolve/GeneralResolveTestClass");
  }

  @Test
  public void test_symbolTable_resolve_TypeVariableShadowingTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/resolve/TypeVariableShadowingTestClass");
  }

  // package symbolTable.typeArgumentsAndParameters.*
  @Test
  public void test_TypeArgumentsAndParameters_TypeArgumentTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/typeArgumentsAndParameters/TypeArgumentTestClass");
  }

  @Test
  public void test_TypeArgumentsAndParameters_TypeParameterTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/typeArgumentsAndParameters/TypeParameterTestClass");
  }

  // package symbolTable.*
  @Test
  public void test_symbolTable_ScopesSymbolTableTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/ScopesSymbolTableTestClass");
  }

  @Test
  public void test_BasicTestClasses_VariablesTestClass() {
    testParsabilityOfModel("src/test/resources/parsableAndCompilableModels",
        "symbolTable/VariablesTestClass");
  }
}
