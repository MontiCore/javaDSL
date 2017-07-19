package typeSystemTestModels.invalid.statements;

import java.io.IOException;

public class ThrowIsValid {
  public void test(){
    if(true) {
      throw new Integer(10);
    }
  }
}
