package typeSystemTestModels.valid.interfaces;

public class C {

  public int getString(){
    return 1;
  }

  protected int getString(int n) {
    return 2;
  }

  private int getString(int m, int n) {
    return 3;
  }

  int getString(int... names) {
    return 4;
  }

}