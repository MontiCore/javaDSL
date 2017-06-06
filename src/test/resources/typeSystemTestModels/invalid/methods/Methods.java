package typeSystemTestModels.invalid.methods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.lang.String;

public class Methods<T> extends ArrayList{

  T id(T t) {

  }

  public void setName(){

  }

  protected void setName1(){

  }

  public List<String> getList(){
    return new ArrayList<String>();
  }

  public void addName(){

  }

  List<String> toList(Collection<String> c) {
    return new ArrayList<String>();
  }

}