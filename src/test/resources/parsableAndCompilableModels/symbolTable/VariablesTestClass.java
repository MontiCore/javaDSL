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

package symbolTable;

public class VariablesTestClass {

  static int staticValue;

  final int finalValue = 0;

  // modifiers
  public int publicValue;

  protected int protectedValue;

  // primitive type
  int integerValue;

  int[] integerArray;

  int integerArrayWithTrailingBrackets[];

  int[] integerMatrixWithLeadingAndTrailingBrackets[];

  // Reference
  VariablesTestClass someReference;

  VariablesTestClass[] someReferenceArray;

  VariablesTestClass someReferenceArrayWithTrailingBrackets[];

  VariablesTestClass[] someReferenceMatrixWithLeadingAndTrailingBrackets[];

  private int privateValue;

  // different variable declarations
  void someMethod(int someMethodParameter) { // parameter

    // local variable
    int localVariable;

    // for init variable
    for (int i = 0; i < 10; i++) {
    }

    // resource
    try (ClosableImplementingResource autoClosableResource = new ClosableImplementingResource()) {
    }
    // Exception
    catch (Exception anException) {
    }
    finally {
    }
    try {
    }
    catch (ArrayOutOfBoundsException | NullPointerException multiCatchException) {
    }
  }
}

class ClosableImplementingResource implements AutoCloseable {
  public void close() throws Exception {
  }
}
