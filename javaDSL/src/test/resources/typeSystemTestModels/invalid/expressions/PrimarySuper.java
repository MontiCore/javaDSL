/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.expressions;

public interface PrimarySuper extends SuperClass{

  int m = super.aa;

  public class Object {
    int n = super.aa;

  }


}
