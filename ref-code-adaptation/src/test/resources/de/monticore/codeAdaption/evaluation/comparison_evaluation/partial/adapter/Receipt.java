package evaluationcomparison;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Receipt"}, template = "${}")
public class Receipt {
  @Adapt(ref = {"Receipt.code"}, template = "${}")
  public int code() {
    return 11;
  }
}
