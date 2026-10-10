/* (c) https://github.com/MontiCore/monticore */
package invalid;

import java.util.List;

class Expressions {

  void expressionStatements(String s, int i, List<String> names) {
    s.length();
    names.add(s);
    s.unknown();                                // error
    names.add(i);                               // error
    undefined(i);                               // error
    i = s;                                      // error
  }
}
