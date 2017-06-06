package typeSystemTestModels.valid.classes;

import java.io.Serializable;
import java.lang.Integer;
import java.util.List;

public class ClassMethodSignatureNoOverrideEquivalent{

  public void setName(){

  }

  public void setName(int n) {


  }

  public void setName(Integer n) {

  }


  public void setName(List<Integer> n){

  }

  public <T extends  Serializable & List<Integer>> void setName(T t){

  }




}