package typeSystemTestModels.valid.enums;

import java.lang.String;

public class EnumToFinalizerMethods {

  public enum DayFood {
    MONDAY ("apple");
    private String food;
    private DayFood(String s) {
      this.food = s;
    }
  }
}
