/* (c) https://github.com/MontiCore/monticore */

package simpleTestClasses;

import java.util.function.Function;

public class VarVariables {
  public boolean varTest() {
    var x = 42;
    final var y = 42 + 1;
    
    int var = 42 + 2;
    
    for (var i = 0; i < x; i++) {
      var j = i * 2;
    }
    
    Function<String, String> q = (var p) -> "Hello " + p;
  }
}
