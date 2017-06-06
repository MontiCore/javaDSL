package typeSystemTestModels.invalid.expressions;

public class UtilUtil<T> {


  public static <K, V> boolean compare(Pair<K, V> a, Pair<K, V> b) {
    return false;
  }

  public static <K, V> boolean compare(K c, V d) {
    return false;
  }

  public static int getPrimitive(){
    return 1;
  }

}