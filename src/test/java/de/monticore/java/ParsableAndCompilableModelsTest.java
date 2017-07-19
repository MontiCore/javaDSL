/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.monticore.java;

import org.junit.Ignore;
import org.junit.Test;

/**
 * A simple test for java tool.
 *
 * @author (last commit) $Author$
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
