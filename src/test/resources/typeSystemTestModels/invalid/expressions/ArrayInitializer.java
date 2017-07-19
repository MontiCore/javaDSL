package typeSystemTestModels.invalid.expressions;

import java.util.ArrayList;
import java.lang.String;

public class ArrayInitializer {
  ArrayList<String>[][] arr = {new ArrayList<String>(), new ArrayList<String>()};
  ArrayList<String>[][] arr1 = {{new ArrayList<String>(), new ArrayList<String>()},{new ArrayList<String>(), new ArrayList<String>()}};
}
