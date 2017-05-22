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

import java.io.IOException;

import org.antlr.v4.runtime.RecognitionException;
import org.junit.Test;

/**
 * A simple test for java tool.
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public class UnparsableModelsTest extends AbstractTestClass {

  @Test
  public void testBasicCompilationUnitMissingBracket() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/BasicCompilationUnitMissingBracket.java");
  }

  @Test
  public void testTwoTimesClass() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/TwoTimesClass.java");
  }

  @Test
  public void testWrongExpression() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/WrongExpression.java");
  }

  @Test
  public void testWrongIdentifierName() throws RecognitionException, IOException {
    testUnparsabilityOfModel("src/test/resources/unparsableModels/WrongIdentifierName.java");
  }

}
