package simpleTestClasses;

public class QualifiedNameTestClass {

  QualifiedNameTestClass someReference;

  InnerClass referenceWithAnInnerClassType;

  GenericInnerClass<InnerClass> referenceWithAnInnerClassTypeAndGenericType;

  class InnerClass {
  }

  class GenericInnerClass<T> {
  }
}
