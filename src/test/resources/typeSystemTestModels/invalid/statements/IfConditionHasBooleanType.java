package typeSystemTestModels.invalid.statements;

import java.lang.Integer;
import java.lang.Object;

public class IfConditionHasBooleanType {

  public void test(){
    int[] n = new int[10];
    if(n.length == "haha") {

    }
    if(n[1] = 1) {

    }
    Integer integer;

    if(integer instanceof Object) {

    }
  }
}
