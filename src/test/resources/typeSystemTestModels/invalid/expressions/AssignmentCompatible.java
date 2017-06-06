package typeSystemTestModels.invalid.expressions;

import java.lang.String;

public class AssignmentCompatible {
  int n = 1;
  char c = 1;


  public void TestInvalidAssignment(){
    int[] a = {1,2,4};
    a[1] = 3;
    n = 1.2;
    c = 1 + 2 + 1.2;
    g = 2;//throw error
    getName() = 1;
  }

  public String getName(){
    int n = 1;
    double d = 1.2;
    float f = 1.2f;
    long l = 1L;

  }

}