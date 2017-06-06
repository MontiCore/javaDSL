package typeSystemTestModels.invalid.methods;

public class MethodNoAccessPairModifier {

  public private protected void setName(){

  }

  public private  void setName1(){

  }

  public protected void setName2(){

  }

  private protected void setName3(){

  }

}