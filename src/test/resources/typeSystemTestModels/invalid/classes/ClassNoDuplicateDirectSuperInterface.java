package typeSystemTestModels.invalid.classes;

import typeSystemTestModels.models.A;
import typeSystemTestModels.models.B;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;
import java.lang.Number;

public class ClassNoSuperInterfaceDuplicate implements List<String>, List<Number> {

  public class InnerClass extends ArrayList<String> implements List<Integer> {

  }

}