package symbolTable.typeArgumentsAndParameters;

interface SomeInterface {
}

public class TypeParameterTestClass<T extends SomeClass & SomeInterface, S extends AnotherInterface<S>> {
  <U> U genericMethod(U u) {
    return u;
  }

  class InnerClass<E extends T> {
    E method() {
      return null;
    }
  }
}

class SomeClass {
}

class AnotherInterface<T> {
}
