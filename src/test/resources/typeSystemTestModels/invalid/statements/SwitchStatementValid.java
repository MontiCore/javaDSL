package typeSystemTestModels.invalid.statements;

import java.lang.String;
import java.lang.Double;

public class SwitchStatementValid {
  public void test(){
    int month = 8;
    Double d;
    String monthString;
    switch (d) {
      case 1:  monthString = "January";
        break;
      default: monthString = "Invalid month";
        break;
      default: monthString = "Another month";
        break;
    }

    WeekDay day;
    switch (day) {
      case WED:  monthString = "January";
        break;
      case MONDAY:  monthString = "January";
        break;
      case TUESDAY:  monthString = "January";
        break;
    }
  }

  public enum WeekDay {
    MONDAY,
    TUESDAY
  }
}
