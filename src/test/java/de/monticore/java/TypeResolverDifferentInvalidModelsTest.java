/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
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

import java.util.Arrays;
import java.util.Collection;

import de.se_rwth.commons.logging.LogStub;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.java.types.JavaDSLTypeChecker;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

/**
 *  on 19.08.2016.
 */
public class TypeResolverDifferentInvalidModelsTest extends AbstractCoCoTestClass{

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
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0707 method 'getName' is duplicated in interface 'SymbolReference'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/SymbolReference", typeChecker.getAllTypeChecker(),expectedErrors);
  }

  @Test
  public void testSymbol(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0709 method 'getPackageName' is declared native in interface 'Symbol'."),
        Finding.error(
            "0xA0712 method 'getFullName' is declared final in interface 'Symbol'."),
        Finding.error("0xA0713 method 'getName' is declared static in interface 'Symbol'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/Symbol", typeChecker.getAllTypeChecker(),expectedErrors);
  }

  @Test
  public void testTypeSymbol(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0703 interface 'TypeSymbol' must extend only interface.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/TypeSymbol", typeChecker.getAllTypeChecker(), expectedErrors);
  }

  @Test
  public void testSymbolKind(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0705 inner class 'SymbolKind1' must not be named same as enclosing interface."),
        Finding.error("0xA0707 method 'getName' is duplicated in interface 'SymbolKind'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/SymbolKind", typeChecker.getAllTypeChecker(),expectedErrors);
  }

  @Test
  public void testTypeSymbolKind(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0202 class is not extending class in declaration of 'TypeSymbolKind'."),
        Finding.error("0xA0913 boolean type of method cannot have return type java.lang.String."),
        Finding.error("0xA0206 method 'getName' of the interface 'SymbolKind' must be implemented in class 'TypeSymbolKind."),
        Finding.error("0xA0206 method 'getName' of the interface 'SymbolKind' must be implemented in class 'TypeSymbolKind.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/TypeSymbolKind", typeChecker.getAllTypeChecker(),expectedErrors);
  }


  @Test
  public void testAmbiguityException(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0509 type 'java.lang.String' cannot be converted to type 'java.lang.String'."));
    testModelNoErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/AmbiguityException");
  }

  @Test
  public void testAstAdditionalAttributes(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0509 type 'boolean' cannot be converted to type 'java.lang.String'."),
        Finding.error("0xA0913 java.lang.Integer type of method cannot have return type java.lang.String."));
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/AstAdditionalAttributes", typeChecker.getAllTypeChecker(),expectedErrors);
  }

  @Test
  public void testEmfAttribute(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0913 java.lang.String type of method cannot have return type java.util.ArrayList."),
        Finding.error("0xA0917 switch expression in the switch-statement must be char, byte, short, int, Character,\n"
            + "Byte, Short, Integer, or an enum type."),
        Finding.error("0xA0916 type of the case-label is incompatible with the type of the switch-statement."),
        Finding.error("0xA0916 type of the case-label is incompatible with the type of the switch-statement.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/EmfAttribute", typeChecker.getAllTypeChecker(),expectedErrors);
  }


  @Test
  public void testLiteralsHelper(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0909 condition in if-statement must be a boolean expression."),
        Finding.error("0xA0909 condition in if-statement must be a boolean expression."),
        Finding.error("0xA0918 exception in throw-statement must be Throwable or subtype of it."),
        Finding.error("0xA0574 method 'parseInt' is not found."),
        Finding.error("0xA0509 type 'boolean' cannot be converted to type 'int'.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/LiteralsHelper", typeChecker.getAllTypeChecker(),
        expectedErrors);
  }

  @Test
  public void testMC2CDStereotypes(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0608 The modifier 'private' is mentioned more than once in the field declaration."),
        Finding.error("0xA0810 method 'toString' must specify a body.")
    );
    testModelForErrors("src/test/resources",
        "typeSystemTestModels/monticore/invalid/MC2CDStereotypes", typeChecker.getAllTypeChecker(),
        expectedErrors);
  }
  @Test
  public void testHelloWorld(){
    Collection<Finding> expectedErrors = Arrays.asList(
        Finding.error("0xA0574 method 'println' is not found."),
        Finding.error("0xA0574 method 'printlnx' is not found.")
    );
    testModelForErrors("src/test/resources",
        "parsableButInvalidModels/HelloWorld", typeChecker.getAllTypeChecker(),
        expectedErrors);
  }

}
