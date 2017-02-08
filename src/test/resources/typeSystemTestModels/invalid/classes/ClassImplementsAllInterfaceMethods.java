package typeSystemTestModels.invalid.classes;

import java.util.List;

public class ClassImplementsAllInterfaceMethods implements List {

  private String getLetter() {
    return "haha";
  }
}