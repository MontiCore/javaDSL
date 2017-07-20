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

interface SomeInterface {
}

class SomeReturnType {
}

public class ScopesSymbolTableTestClass {
  int field;

  {
    // shadowing
    String field;
  }

  SomeReturnType someMethod() {
    class MethodLocalClass implements SomeInterface {
      int methodLocalClassField;
    }
    SomeInterface someVariable;
    {
      SomeInterface someVariable2;
    }
    if (true) {
      SomeInterface variableInIfClause;
    }
    else {
      SomeInterface variableInElseClause;
    }
    for (int i = 0; i < 10; i++) {
      SomeInterface variableInForLoop;
    }
    for (int k = 0; k < 10; k++)
      System.out.println(k); // no scope here

    synchronized (someVariable) {
      SomeInterface variableInSynchronizedBlock;
    }
    while (false) {
      SomeInterface variableInWhileLoop;
    }
    try {
      SomeInterface variableInTryClause;
    }
    catch (Exception e) {
      SomeInterface variableInCatchClause;
    }
    finally {
      SomeInterface variableInFinallyBlock;
    }
    return null;
  }

  class InnerClass {
    int innerClassField;
  }
}
