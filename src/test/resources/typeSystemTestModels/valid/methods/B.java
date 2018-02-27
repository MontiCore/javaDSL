/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.methods;

import typeSystemTestModels.valid.interfaces.C;
import typeSystemTestModels.valid.methods.A;

public class B extends C {

//  int n = getString();
 // int nn = getString(1);

  public class BB extends A {
   // int n  = getString();
    int nn = getName();
//    String s = getHaha();
//    int nnn = getName(1,2,3);
//    int nnnn = getString();
//    int nnnnn = getString(1);
   // int nnnnnn = getString();
  }

  public String getHaha(){
    return "haha";
  }

  public String getHaha(String haha) {
    return "haha";
  }

  public String getHaha(String... haha) {
    return "haha";
  }
}
