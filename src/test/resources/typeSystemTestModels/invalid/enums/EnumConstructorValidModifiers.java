package typeSystemTestModels.invalid.enums;

import java.lang.String;

public class EnumConstructorValidModifiers {

  public enum DayFood {
    MONDAY ("apple"),
    TUESDAY ("orange");

    private String food;
    public DayFood(String s) {
      this.food = s;
    }
  }

  public enum DayMonth{
    JANUARY ("chicken"),
    FEBRUARY ("pork");

    private String food;
    protected DayMonth(String s) {
      this.food = s;
    }
  }
}