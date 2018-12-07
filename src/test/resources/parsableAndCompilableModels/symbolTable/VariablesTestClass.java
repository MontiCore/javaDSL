/* (c) https://github.com/MontiCore/monticore */

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
