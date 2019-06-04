/* (c) https://github.com/MontiCore/monticore */

package stressfulPackage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Set;

/*
 * Compilation Unit with random valid syntax features, but as stressful as possible. 
 */
public strictfp class StressfulSyntax<T extends Object, S extends StressfulSyntax<T, S>> implements
    OtherClass.StressfulInterface<T> {
  public static Boolean b, b2 = false;

  public static Set<? super Integer> set;

  private static int someVal, otherVal = 0;

  static {
    // control flow
    someVal = 0;
    switch (someVal) {
      case 0:
        System.out.println("it's zero!");
        break;
      case 1:
        System.out.println("it's one!");
        break;
      default:
        System.out.println("it's something else!");
    }

    do {
      ;
    } while (false);
    while (b)
      ;
    label:
    for (b = true; b; b = !b, someVal++) {
      continue label;
    }
    for (Object o : new Object[] {}) {
    }
    if (b)
      ;
    if (b) {
    }
    else
      ;
  }

  static {
    EnumInInterfaceInClass x = EnumInInterfaceInClass.NOTHING;
    int[][] arrays[] = new int[][][] { { {} }, { { 1, 2, 3 } } };
    Object[][][] arrays2 = new Object[123][][];
  }

  StressfulSyntax(@StressfulAnnotation Object o) {
    this.b = !(o == null);
  }

  public static synchronized final native void randomHeapOfMethodModifiers();

  @Override
  @StressfulAnnotation
  public Set<Set<? extends T>> stressfulMethod(@StressfulAnnotation final int i) {
    return Collections.emptySet();
  }

  @Retention(RetentionPolicy.RUNTIME) @interface StressfulAnnotation {
    String value() default "some default";
  }
}

class OtherClass {
  strictfp interface StressfulInterface<T> {
    public Set<Set<? extends T>> stressfulMethod(@StressfulSyntax.StressfulAnnotation final int i);

    enum EnumInInterfaceInClass {
      NOTHING(0) {
        @Override int method()[][] {
          // TODO Auto-generated method stub
          return null;
        }
      },
      SOMETHING(1);

      private int x;

      EnumInInterfaceInClass(int x) {
        this.x = x;
      }

      int[] method()[] {
        String[] args;
        return null;
      }
    }
  }
}
