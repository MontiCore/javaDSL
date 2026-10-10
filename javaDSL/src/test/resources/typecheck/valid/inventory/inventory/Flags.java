/* (c) https://github.com/MontiCore/monticore */
package inventory;

/** Bit flags of an inventory entry. */
public final class Flags {

  public static final int RESERVED = 1;

  public static final int DAMAGED = 1 << 1;

  public static final int ON_SALE = 1 << 2;

  private static final String[] NAMES;

  static {
    NAMES = new String[] {"reserved", "damaged", "on sale"};
  }

  private Flags() {
  }

  public static boolean isSet(int flags, int flag) {
    return (flags & flag) != 0;
  }

  public static int set(int flags, int flag) {
    return flags | flag;
  }

  public static int clear(int flags, int flag) {
    return flags & ~flag;
  }

  public static String describe(int flags) {
    StringBuilder result = new StringBuilder();
    for (int bit = 0; bit < NAMES.length; bit++) {
      if (((flags >>> bit) & 1) == 1) {
        if (result.length() > 0) {
          result.append(", ");
        }
        result.append(NAMES[bit]);
      }
    }
    return result.toString();
  }
}
