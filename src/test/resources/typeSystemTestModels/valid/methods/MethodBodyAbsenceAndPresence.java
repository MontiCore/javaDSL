package typeSystemTestModels.valid.methods;

public class MethodBodyAbsenceAndPresence {

  public abstract String getString();

  public native String getString1();

  public static String getString2(){
    return  "haha";
  }



}
