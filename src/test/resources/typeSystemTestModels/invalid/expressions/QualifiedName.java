package typeSystemTestModels.invalid.expressions;

import java.lang.String;

public class QualifiedName extends String {

  int j = 1;
  int m  = 1;
  int m  = 2;
  int n = this.m;
  int o = m;
  int g = 1;
  int ff = g;

  D d = D.MONDAY;
  D d = D.h;
  String[] intArr = new String[ff];

  public enum D{
    MONDAY,
    TUESDAY
  }

  public void checkArray(){
    int arrLength = intArr.length;
    int arrField = intArr.aa;
  }




}