package typeSystemTestModels.valid.expressions;

public class PrefixOp {


  public void TestPrefixOp(){
    int[] nn = {1,2};
    ++nn[0];
    int n = 1;
    ++n;
    double d = 1.2;
    --d;
  }
}