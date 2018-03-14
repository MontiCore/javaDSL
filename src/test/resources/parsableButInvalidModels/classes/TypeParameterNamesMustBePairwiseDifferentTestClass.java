/* (c) https://github.com/MontiCore/monticore */

package classes;

public class TypeParameterNamesMustBePairwiseDifferentTestClass<T, T> {
  <S, S> void method() {
  }
}
