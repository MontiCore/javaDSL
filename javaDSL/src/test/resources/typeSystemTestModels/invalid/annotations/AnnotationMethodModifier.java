/* (c) https://github.com/MontiCore/monticore */

package resources.typeSystemTestModels.invalid.annotations;

public class AnnotationMethodModifier {

  @interface TestInvalid{
    final String getName();
    native String getName1();
    private String getName2();
    protected String getName3();
    static String getName4();
    strictfp String getName5();
    synchronized String getName6();
    transient String getName7();
    volatile String getName8();
  }
}
