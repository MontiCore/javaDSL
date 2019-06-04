/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.interfaces;

public interface InterfaceInnerTypeNotNamedAsEnclosing {

  public interface InterfaceInnerTypeNotNamedAsEnclosing {
  }

  public interface A {

    public class A {

    }
  }

}
