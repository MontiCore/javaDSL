/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.valid.enums;

import java.lang.String;

public class EnumConstructorValidModifiers {
  public enum DayFood {
    MONDAY ("apple"),
    TUESDAY ("Apple");

    private String food;
    private DayFood(String s) {
      this.food = s;
    }
  }
}
