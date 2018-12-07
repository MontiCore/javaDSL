/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.fieldsAndLocalVars;

public class Box<T> {

  public T data;

  public Box(T data) { this.data = data; }

  public void setData(T data) {
    System.out.println("Node.setData");
    this.data = data;
  }
}
