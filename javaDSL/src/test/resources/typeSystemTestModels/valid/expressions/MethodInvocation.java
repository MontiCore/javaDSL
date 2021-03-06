/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.expressions;


import typeSystemTestModels.invalid.expressions.InnerClass;
import typeSystemTestModels.invalid.expressions.Pair;
import typeSystemTestModels.invalid.expressions.UtilUtil;

import java.lang.String;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

public class MethodInvocation extends UtilUtil{

  String s = new String();

  Pair<Integer, String> p1 = new Pair<>(1, "apple");

  Pair<Integer, String> p2 = new Pair<>(2, "pear");

  public int getName() {
    return 1;
  }

  public int getName(int n) {
    return 1;
  }

  public int getName(Integer m, Integer n) {
    return 2;
  }

  public int getName(String s, int... names) {
    return 4;
  }

  public void testMethodInvocation(){
    // only through name
    int n = getName("haha", 1, 2, 3, 4, 5);
  }
  static <T> T pick(T a1, T a2) {
    return a2;
  }


}
