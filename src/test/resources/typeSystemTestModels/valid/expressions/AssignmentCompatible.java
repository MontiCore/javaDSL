package typeSystemTestModels.valid.expressions;

public class AssignmentCompatible {
  int n = 1;

  public void TestAssignment(){
    n = 1;
    n = 1 + 'a' + 3;
    byte b = 1;
    char c = 'c';
    n = b + c ;

  }

}
