/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.Integer;
import java.lang.String;
import java.util.Collection;

public class MethodOverride extends Methods<String> {

  Object id(Object o) {
    return new Object();
  }

  @Override
  public List<Integer> getList() {
    return super.getList();
  }

  @Override
  public double size() {
    return 100;
  }

  protected void setName(){

  }

  private void setName1(){

  }

  List toList(Collection c) {
    return new ArrayList();
  }
}
