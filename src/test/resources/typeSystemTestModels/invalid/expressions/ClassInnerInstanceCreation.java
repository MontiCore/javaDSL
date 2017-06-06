package typeSystemTestModels.invalid.expressions;

public class ClassInnerInstanceCreation {

  InnerClass.Day d = InnerClass.new Day() {
  };

  InnerClass.C c = InnerClass.new C(){

  };

  InnerClass.A a = InnerClass.new A();
  InnerClass.B b = InnerClass.new B();
  InnerClass.DayDay dd = InnerClass.new DayDay();
  InnerClass.E e = InnerClass.new E();

}