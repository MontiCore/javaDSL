/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.methods;

public abstract class MethodAbstractAndOtherModifiers {

  abstract private String getName();

  abstract static String getName1();

  abstract final String getName2();

  abstract native String getName3();

  abstract strictfp String getName4();

  abstract synchronized String getName5();



}
