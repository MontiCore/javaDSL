/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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

import de.se_rwth.commons.logging.LogStub;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.monticore.java.types.JavaDSLTypeChecker;
import de.se_rwth.commons.logging.Log;

/**
 * Created by Odgrlb on 19.08.2016.
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
