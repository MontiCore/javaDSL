/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.enums;

import java.lang.String;

public class EnumToFinalizerMethod {

  public enum DayFood {
    MONDAY ("apple");

    private String food;
    private DayFood(String s) {
      this.food = s;
    }

    protected void finalize()  {
    }
  }
}
