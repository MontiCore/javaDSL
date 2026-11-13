public class RecordFields {
  int x;
  int record;
  
  record Point(int x, int y){};
  
  public RecordFields(int x, int y) {
    this.x = x;
    this.record = y;
  }
}