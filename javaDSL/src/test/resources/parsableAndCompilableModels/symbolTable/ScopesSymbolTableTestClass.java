/* (c) https://github.com/MontiCore/monticore */

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
