package typeSystemTestModels.valid.fieldsAndLocalVars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.lang.Object;
import java.lang.String;
import java.lang.Integer;
import java.lang.Number;
import java.lang.Double;

public class CastConversion {

  Object o = new Object();
  String s = (String) o;

  double dd = 2.4;
  int n = (int) dd;

  Number num = dd;
  double d = (Double)num;

  Number nn = new Integer(5);
  Double n = (Double)nn;

  Serializable s = (Serializable)o;

  Serializable c = (Serializable) this;

}