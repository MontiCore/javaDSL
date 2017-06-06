package typeSystemTestModels.valid.statements;

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
    for(int nn : n) {

    }
    for(Integer d : n){

    }
    for(String s : l) {

    }
    for(Object o : ll){

    }
  }
}