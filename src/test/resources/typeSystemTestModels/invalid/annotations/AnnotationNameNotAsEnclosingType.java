package resources.typeSystemTestModels.invalid.annotations;

public class AnnotationNameNotAsEnclosingType {

  @interface AnnotationNameNotAsEnclosingType {

  }

  public class A {

    @interface A {

    }

    @interface AnnotationNameNotAsEnclosingType {

    }
  }

}
