/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.methods;

public class MethodBodyAbsenceAndPresence {

  private String getString();

  abstract String getString1(){

  }

  native String getString2(){

  }
}
