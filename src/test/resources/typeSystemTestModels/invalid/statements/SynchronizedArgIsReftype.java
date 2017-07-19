package typeSystemTestModels.invalid.statements;

public class SynchronizedArgIsReftype {
  public void test(){
    int n = 1;
    synchronized (n) {
      synchronized (1.2) {

      }
    }
  }
}
