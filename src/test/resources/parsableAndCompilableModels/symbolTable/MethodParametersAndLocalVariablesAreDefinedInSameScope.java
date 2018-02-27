/* (c) https://github.com/MontiCore/monticore */

package symbolTable;

public class MethodParametersAndLocalVariablesAreDefinedInSameScope {
  int n = 0;
  int m = 1;

  public void testMethod(int param){
    int sum = m + n;
    int sumsum = 1 + sum;
    sum = 1;
  }
}
