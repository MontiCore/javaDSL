package typeSystemTestModels.invalid.statements;

import java.lang.Integer;
import java.lang.Double;
import java.util.ArrayList;
import java.util.List;
import java.lang.String;

public class ForEachIsValid {
  int[] n = {1,2,3};
  List<String> l = new ArrayList<>();
  List ll = new ArrayList();
  Integer integer ;
  public void  testForEach(){
    for(int nn : integer) {

    }
    for(Double d : n){

    }
    for(Integer s : l) {

    }
    for(Object o : ll){

    }
  }
}
