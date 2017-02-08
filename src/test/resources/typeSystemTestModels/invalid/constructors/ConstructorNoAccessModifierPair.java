package typeSystemTestModels.invalid.constructors;

public class ConstructorNoAccessModifierPair {

  public protected private ConstructorNoAccessModifierPair() {

  }

  public protected ConstructorNoAccessModifierPair(){

  }

  public private ConstructorNoAccessModifierPair(){

  }

  private protected ConstructorNoAccessModifierPair(){

  }

  public ConstructorNoAccessModifierPair(){
    for(int i = 0 ; i < 10;i++){
      break;
    }
  }
}

