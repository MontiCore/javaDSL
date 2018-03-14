/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.statements;

import java.io.IOException;

public class ThrowIsValid {
  public void test(){
    if(true) {
      throw new Integer(10);
    }
  }
}
