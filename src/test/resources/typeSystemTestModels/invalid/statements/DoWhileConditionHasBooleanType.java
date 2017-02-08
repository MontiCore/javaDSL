package typeSystemTestModels.invalid.statements;

public class DoWhileConditionHasBooleanType {
  public void testDoWhile(){
    int count = 1;
    do {
      count++;
    } while (count);
  }
}