package typeSystemTestModels.invalid.expressions;

import java.io.Serializable;
import java.lang.Integer;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.lang.Object;

public class MethodInvocation extends UtilUtil<Integer> {

  String s = new String();

  Util util = new Util();

  UtilUtil<Integer> u = new UtilUtil();

  Pair<Integer, String> p1 = new Pair<>(1, "apple");

  Pair<Integer, String> p2 = new Pair<>(2, "pear");

  public int getName() {
    return 1;
  }

  public int getName(int n) {
    return 1;
  }

  public int getName(Integer m, Integer n) {
    return 2;
  }

  public int getName(String s, int... names) {
    return 4;
  }

  public void TestMethodInvocation() {
 //    only through name
//    int n = getName("haha", 1, 2, 3, 4, 5);
//    n = getName();
//    n = getName(1);
//    n = getName(1, 1);
    int nn = add(1);
    //
    n = s.getSecondInt();
    int m = InnerClass.getInt();
    m = super.getPrimitive();
    //Generic Invocation
    boolean same = this.<Integer, String>compare(p1, p2);

    boolean same3 = super.<String, Integer>compare("hello", 1);
    this.<Integer>add(new Integer(10));

    //    TypeName.identifier
    boolean same1 = Util.compare(p1, p2);

    boolean same2 = Util.<String, Integer>compare("hello", 1);
    //    FieldName.identifier
    boolean same7 = util.compare(p1, p2);



    //    FieldName.Identifier
   // ClassName.super.Identifier
    boolean same4 = MethodInvocation.super.compare(p1, p2);  //error
    boolean same6 = u.compare(p1, p2); //error
////    super.Identifier
    boolean same5 = super.compare(p1, p2);  //error


    Serializable s1 = pick("d", new ArrayList<String>());
    String s2 = pick("String", 1);

    List<String> s3 = new ArrayList<String>();
    Object o = pick("haha", s3);
//
    Object objectElement = new Object();
    List<String> stringList = new ArrayList<String>();
    Object theElement = addAndReturn(objectElement, stringList);
  }

  public <T extends Object> void add(T t) {
  }

  static <T> T pick(T a1, T a2) {
    return a2;
  }

  public static <T> T addAndReturn(T element, Collection<T> collection) {
    return element;
  }
}