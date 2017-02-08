package symbolTable.resolve;

class SuperClass {
  private void privateSuperMethod() {
  }

  protected void protectedSuperMethod() {
  }
}

public class GeneralResolveTestClass<TYPE_VARIABLE> extends SuperClass {
  public TYPE_VARIABLE someReference;

  private int x = 0;

  public GeneralResolveTestClass(int initialX) {
    x = initialX;
  }

  void variableShadowingMethod() {
    x++;
    String x = "Hello";
    x += " World!";
  }
}
