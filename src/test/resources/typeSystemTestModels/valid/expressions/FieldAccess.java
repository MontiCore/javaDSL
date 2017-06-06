package typeSystemTestModels.valid.expressions;

public class FieldAccess extends SuperClass {
  int m = 1;
  int a = super.d;
  int b = this.m;
  int aa = d;
  Day day = Day.MONDAY;
  DayDay dd = DayDay.MONDAY;

  public enum Day {
    MONDAY,
    TUESDAY
  }

  public enum DayDay {
    MONDAY,
    TUESDAY
  }


}