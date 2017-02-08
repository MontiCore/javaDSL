package typeSystemTestModels.invalid.expressions;

import java.util.ArrayList;

public class ClassInstanceCreation{
  ArrayList<String> a = new ArrayList<?>();
  Day d = new Day();
  B b = new B();

  public abstract class B {

  }

  public interface HelloWorld {
    public int greet();
  }

   Day frenchGreeting = new Day() {
    public int greet() {
      return 1;
    }
  };

  public enum Day {
    MONDAY,
    TUESDAY
  }

  public final class A{

  }

  A a = new A(){
    public int greet(){
      return 1;
    }
  };

}