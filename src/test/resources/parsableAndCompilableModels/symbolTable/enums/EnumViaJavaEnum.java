/* (c) https://github.com/MontiCore/monticore */

package symbolTable.enums;

public enum EnumViaJavaEnum {
  CONSTANT1() {
    @Override
    public int method() {
      return 1;
    }
  },

  CONSTANT2() {
    @Override
    public int method() {
      return 2;
    }
  };

  abstract int method();
}

// This is needed for testing since java 1.7 sources are not available
@interface Override {

}
