package typeSystemTestModels.valid.annotations;

import java.lang.Class;
import java.lang.String;

public class AnnotationMethodReturnType {

  @interface ValidMethodReturnType {
    //primitive
    int getCount();
    //String
    String getString();
    enum Level { BAD, INDIFFERENT, GOOD }
    Level value();
    //class
    Class<? extends String> getValue();
    AnnotationExample getExample();
  }

  @interface AnnotationExample {
    int getNumber();
  }


}