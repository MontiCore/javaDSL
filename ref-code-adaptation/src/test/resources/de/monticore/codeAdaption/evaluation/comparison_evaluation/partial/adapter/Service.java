package evaluationcomparison;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Service"}, template = "${}")
public class Service {
  @Adapt(ref = {"Service.amount"}, template = "${}")
  public int amount() {
    return 7;
  }
}
