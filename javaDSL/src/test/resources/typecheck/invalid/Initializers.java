/* (c) https://github.com/MontiCore/monticore */
package invalid;

import java.util.ArrayList;
import java.util.List;

class Initializers {

  int number = "one";                           // error: 0xA0921
  String text = 1;                              // error: 0xA0921
  List<String> names = new ArrayList<>();
  long total = number;

  void locals() {
    long l = 1;
    int i = l;                                  // error: 0xA0921
    boolean b = i;                              // error: 0xA0921
    Object o = names;
    String s = o;                               // error: 0xA0921
    double d = i;
  }
}
