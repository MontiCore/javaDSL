/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.expressions;

import java.util.ArrayList;

public class ArrayAccess {

  public void testValidAccess(){
    int[] intArray = new int[1];
    intArray[0] = 1;
    ArrayList[] list = new ArrayList[1];
    list[1] = new ArrayList();
  }
}
