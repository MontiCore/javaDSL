package evaluationcomparison;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Service"}, template = "${}")
public class Service {
  private ServiceSupport support = new ServiceSupport();

  @Adapt(ref = {"Service.amount"}, template = "${}")
  public int amount() {
    return support.amount();
  }
}
