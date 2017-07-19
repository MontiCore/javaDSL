package typeSystemTestModels.invalid.expressions;

public class Comparison{

  public void TestComparison(){

    if(1.2 < true) {

    }
    if(1 > "String") {

    }
    if(false <= 1) {

    }
    if("String" >= 1) {

    }

  }

}
