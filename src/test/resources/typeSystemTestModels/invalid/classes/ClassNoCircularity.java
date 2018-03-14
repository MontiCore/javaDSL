/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.classes;

public class ClassNoCircularity extends ClassNoCircularity {

  public class A extends C{

  }

  public class B extends A {

  }

  public class C extends B {

  }

}
