package simpleTestClasses.types;

public @interface SimpleAnnotationTestModel {
  String someMethod();

  String someMethodWithDefault() default "Hello World!";
}
