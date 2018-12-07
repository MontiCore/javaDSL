/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.statements;

public class AssertIsValid {

  public void testAssert(){
    boolean b = true;
    assert true;
    assert b = false;
    assert false : getBoolean();

  }

  public boolean getBoolean(){
    return false;
  }

}
