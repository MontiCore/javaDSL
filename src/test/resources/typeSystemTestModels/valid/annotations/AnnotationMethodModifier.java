package typeSystemTestModels.valid.annotations;

import java.lang.String;

public class AnnotationMethodModifier {

  @interface TestValid{
    String getName();
    int getCount();
  }

}
