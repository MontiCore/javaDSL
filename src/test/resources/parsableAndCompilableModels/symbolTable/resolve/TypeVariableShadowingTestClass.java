package symbolTable.resolve;

class T {
}

public class TypeVariableShadowingTestClass<T> {

  T thisIsAnInstanceOfTheInnerClassT = new T();

  TypeVariableShadowingTestClass.T alsoAnInstanceOfTheInnerClassT = new T();

  S anInstanceOfTheInnerClassS = new S();

  <S> void method(S thisIsAnInstanceOfTheTypeVariableSOfThisMethod) {
    class S {
    }
    S thisIsAlsoAnInstanceOfTheTypeVariableSOfThisMethod = null;
  }

  class T {
  }

  class S {
  }
}
