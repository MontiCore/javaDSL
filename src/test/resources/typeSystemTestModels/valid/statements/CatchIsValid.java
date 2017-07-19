package typeSystemTestModels.valid.statements;

import java.io.IOException;
import java.lang.IndexOutOfBoundsException;

public class CatchIsValid {
  public void testCatch(){
    try {

    } catch (IndexOutOfBoundsException e) {
      System.err.println("IndexOutOfBoundsException: " + e.getMessage());
    } catch (IOException e) {
      System.err.println("Caught IOException: " + e.getMessage());
    }
  }
}
