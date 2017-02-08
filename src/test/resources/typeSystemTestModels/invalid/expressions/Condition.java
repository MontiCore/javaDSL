package typeSystemTestModels.invalid.expressions;

public class Condition {
  int b = (1 = 2) ? true : false;
  int c = (1 == 2) ? getName() : false;
  int c = (1 == 2) ?  true : getName();

  public void getName(){

  }
}