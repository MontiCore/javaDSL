package typeSystemTestModels.invalid.fieldsAndLocalVars;

import java.lang.Integer;

public class ChildBox extends Box<Integer> {

  public ChildBox(Integer data) {
    super(data);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void setData(Integer data) {
    System.out.println("MyNode.setData");
    super.setData(data);
  }
}
