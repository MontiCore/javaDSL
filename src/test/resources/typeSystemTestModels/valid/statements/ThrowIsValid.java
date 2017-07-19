package typeSystemTestModels.valid.statements;

import java.io.IOException;

public class ThrowIsValid {
  public void test(){
    if(true){
      throw new IOException();
    }
  }

}
