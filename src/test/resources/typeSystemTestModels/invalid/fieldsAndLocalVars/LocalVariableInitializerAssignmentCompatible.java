package typeSystemTestModels.invalid.fieldsAndLocalVars;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Integer;
import java.lang.String;
import java.lang.Object;

public class LocalVariableInitializerAssignmentCompatible {
  public void test(){
    //for error
    Capture<?> capture = new Capture();//warning

    First<Integer> s = new Third<String>();

    Capture<?> capture1 = new Capture<InputStream>();

    Capture<?> capture2 = new Capture<String>(); // error

    Capture<? extends Serializable> capture2 = new Capture<Serr>();

    Capture<? super FilterInputStream> capture3 = new Capture<String>(); //error

    List<?> list = new ArrayList<String>();

    List<? extends Integer> list = new ArrayList<String>(); //error

    CaptureExample<Object, String> cap = new CaptureExample(); //warning

    CaptureExample<Object, String> cap = new CaptureExample<Object, String>();

    Capture<String> s = new Capture<String>();

    Box<String> param = new Box();
    Box bb = param; //warning

    List<Integer> l = new ArrayList(); //warning

    Box<String> c = new CaptureExample<Object, String>();

    Map<String, List<String>> map = new HashMap(); //warning

    Integer i= 1;
    int ii = i;
    byte b = 1;
  }

}