package typeSystemTestModels.invalid.expressions;

public class ArrayAccess
{
  int[] n = new int[1];
  int[][] nn = new int[1][2];
  int[][][] nn = new int[1][2][3];
  long l = 1;

  public void tesArray(){
    n[1.2] = 1;
    nn[0]["String"] = 1;
    nnn[0][0][true] = 2;
    l[1] = 1;
  }
}
