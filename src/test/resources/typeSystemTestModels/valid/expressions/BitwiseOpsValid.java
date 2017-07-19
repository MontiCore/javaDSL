package typeSystemTestModels.valid.expressions;

public class BitwiseOpsValid{
  int a = 60;
  int b = 13;
  int c = 0;
  boolean d = true;
  boolean e = false;

  public void TestValidBitwiseOpsValid(){
    c = a & b;
    c = a | b;
    c = a ^ b;
    c = d & e;
    c = d & e;
  }
}
