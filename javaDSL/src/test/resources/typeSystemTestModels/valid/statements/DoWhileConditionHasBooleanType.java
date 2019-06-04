/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.statements;

public class DoWhileConditionHasBooleanType {
  public void testDoWhile(){
    int count = 1;
    do {
      count++;
    } while (count < 11);
  }
}
