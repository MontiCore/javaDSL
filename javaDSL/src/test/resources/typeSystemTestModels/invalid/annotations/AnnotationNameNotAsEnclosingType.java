/* (c) https://github.com/MontiCore/monticore */

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
