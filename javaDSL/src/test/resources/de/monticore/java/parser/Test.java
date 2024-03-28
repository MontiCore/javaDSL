package de.monticore.foo.bla;

import java.util.List;
import java.math.BigInteger;

import de.monticore.cd4code.CD4CodeMill;

public class Test {

  public String text = "Hello World!";

  public static String text1 = "Hello World!";

  public final String text2 = "Hello World!";

  public static final String text3 = "Hello World!";

  protected String text4 = "Hello World!";

  protected static String text5 = "Hello World!";

  protected final String text6 = "Hello World!";

  protected static final String text7 = "Hello World!";

  private String text8 = "Hello World!";

  private static String text9 = "Hello World!";

  private final String text10 = "Hello World!";

  private static final String text11 = "Hello World!";

  String text12 = "Hello World!";

  static String text13 = "Hello World!";

  final String text14 = "Hello World!";

  static final String text15 = "Hello World!";

  public int[][] myArray = {{1, 2, 3}, {4, 5, 6}}, mySecondArray = {{1, 2}, {3, 4}, {5, 6, 7, 8}};

  public void hello() {
    System.out.println(text);
  }

  public static void hello1() {
    System.out.println(text1);
  }

  private void hello2() {
    System.out.println(text);
  }

  private static void hello3() {
    System.out.println(text1);
  }

  protected void hello4() {
    System.out.println(text);
  }

  protected static void hello5() {
    System.out.println(text1);
  }

  void hello6() {
    System.out.println(text);
  }

  static void hello7() {
    System.out.println(text1);
  }

  public Test(final int a, String b, List<Boolean> c) {
    a++;
    b = b.substring(2, 3);
    c.add(true);
  }

}