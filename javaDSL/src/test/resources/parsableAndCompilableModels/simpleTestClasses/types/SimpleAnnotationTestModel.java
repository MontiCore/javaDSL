/* (c) https://github.com/MontiCore/monticore */

package simpleTestClasses.types;

public @interface SimpleAnnotationTestModel {
  String someMethod();

  String someMethodWithDefault() default "Hello World!";
}
