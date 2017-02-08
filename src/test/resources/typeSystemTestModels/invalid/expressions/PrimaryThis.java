package typeSystemTestModels.invalid.expressions;

public interface PrimaryThis {

  int n = 1;
  int m = this.n;


}