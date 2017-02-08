package symbolTable.enums;

//This is needed for testing since java 1.7 sources are not available 
@interface Override {

}

public interface EnumViaJavaInterface {
  EnumViaJavaInterface CONSTANT1 = new EnumViaJavaInterface() {
    @Override
    public int method() {
      return 1;
    }
  };

  EnumViaJavaInterface CONSTANT2 = new EnumViaJavaInterface() {
    @Override
    public int method() {
      return 2;
    }
  };

  abstract int method();

  // possibly more methods like values(), valueOf(), ...
  // omitted due to parser bug not parsing static methods
}
