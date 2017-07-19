package typeSystemTestModels.valid.methods;

import java.lang.Double;

public class MethodNoDuplicateModifier {

  public strictfp setDouble(Double d) {

  }

  private synchronized setDouble(Double d) {

  }


}
