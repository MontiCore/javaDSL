package typeSystemTestModels.invalid.expressions;

public class BitwiseOpsValid {

  double a = 1;
  float b = 1;
  String c = "haha";

  public void TestInvalidBitwiseOps(){
    c = a & b;
    c = a | b;
    c = a ^ b;
  }
}
