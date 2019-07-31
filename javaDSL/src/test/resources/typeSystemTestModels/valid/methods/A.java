/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.methods;

public class A {

  public int getName(){
    return 1;
  }

  protected int getName(int n) {
    return 2;
  }

  private int getName(int m, int n) {
    return 3;
  }

  int getName(int... names) {
    return 4;
  }

}
