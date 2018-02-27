/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.methods;

import java.lang.Double;

public class MethodNoDuplicateModifier {

  public strictfp strictfp void setDouble(Double d) {

  }

  public synchronized synchronized void setDouble(Double d) {

  }

  private private void setDouble(Double d)  {

  }
}
