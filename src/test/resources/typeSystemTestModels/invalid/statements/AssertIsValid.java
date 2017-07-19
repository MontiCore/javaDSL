package typeSystemTestModels.invalid.statements;

public class AssertIsValid {

  public void testAsser(){
    boolean  b = true;
    assert b = false;
    assert 1;
    assert false : add();
  }

  public boolean getBoolean(){
    return true;
  }

  public void add(){

  }
}
