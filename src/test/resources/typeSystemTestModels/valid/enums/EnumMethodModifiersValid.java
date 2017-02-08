package typeSystemTestModels.valid.enums;

import java.lang.String;

public class EnumMethodModifiersValid {
  public enum DayFood {
    MONDAY ("apple"),
    TUESDAY ("orange");
    private String food;
    private DayFood(String s) {
      this.food = s;
    }
    public DayFood getTuesday(String s){
      return MONDAY;
    }

    private DayFood getMonday( ){
      return MONDAY;
    }
  }
}