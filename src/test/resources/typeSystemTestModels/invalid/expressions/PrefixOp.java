/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.expressions;

import java.lang.String;

public class PrefixOp {

  public void TestPrefixOp(){
    boolean b = true;
    ++b;
    String s = "haha";
    --s;
  }

}
