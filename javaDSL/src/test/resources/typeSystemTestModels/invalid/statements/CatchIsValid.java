/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.statements;

import java.lang.IndexOutOfBoundsException;
import java.io.IOException;
import java.lang.Integer;

public class CatchIsValid {

  public void testCatch(){
    try {

    } catch (Integer e) {
      System.err.println("IndexOutOfBoundsException: " + e.getMessage());
    } catch (IOException e) {
      System.err.println("Caught IOException: " + e.getMessage());
    }
  }
}
