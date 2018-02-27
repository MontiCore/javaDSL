/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.statements;

public class SynchronizedArgIsReftype {
  public void test(){
    synchronized ("haha") {
      synchronized (new Object()){

      }
    }
  }
}
