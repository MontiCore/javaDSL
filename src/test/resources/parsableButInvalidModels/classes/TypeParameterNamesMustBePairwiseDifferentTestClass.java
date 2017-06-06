package classes;

public class TypeParameterNamesMustBePairwiseDifferentTestClass<T, T> {
  <S, S> void method() {
  }
}
