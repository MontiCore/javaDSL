/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.expressions;

public class AdditiveOps {
  int n = 1 + 2;
  int m = n - 2;
  double a = 1.2;
  double d = 1 + n + a;
  int o = (1 + n) * n;

  public void haha(){
    o = 1 + 2;
  }
}
