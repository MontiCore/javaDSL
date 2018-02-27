/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.expressions;

public class ClassInstanceCreation{
  ArrayList<String> a = new ArrayList<String>();
  ArrayList.Chain c = a.new Chain();

  public interface HelloWorld {
    public int greet();
  }

  HelloWorld frenchGreeting = new HelloWorld() {
    public int greet() {
      return 1;
    }
  };


}
