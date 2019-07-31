/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.expressions;

import java.lang.String;
import java.lang.Integer;
import java.util.List;

public class InstanceOf {

  public void testInstanceOf(){
    Integer storage;
    char c = 'a';
    if(c instanceof int) {

    }
    if(storage instanceof List<Integer>) {

    }
    if(storage instanceof String) {

    }

  }
}
