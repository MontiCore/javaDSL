/* (c) https://github.com/MontiCore/monticore */

package typeSystemTestModels.invalid.expressions;

public class ArrayDimensionByExpressionValid {

  long l = 1;
  int[] n = new int[a];
  int[] m = new int[1.2];
  int[] aa = new int[l];
  int[][] b = new int[true]["hah"];
}
