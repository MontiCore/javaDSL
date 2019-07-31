/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.methods;

import java.lang.String;
import java.lang.Double;

public class MethodNoNativeAndStrictfp {

  public native String getString();

  public strictfp setDouble(Double d) {

  }

}
