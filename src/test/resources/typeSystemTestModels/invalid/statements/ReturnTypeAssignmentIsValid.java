package typeSystemTestModels.invalid.statements;

public class ReturnTypeAssignmentIsValid {

  public void test(){
    return 1;
  }

  public int test1(){

  }

  public int test2(){
    return ;
  }

  public int test3(){
    return "haha";
  }

}