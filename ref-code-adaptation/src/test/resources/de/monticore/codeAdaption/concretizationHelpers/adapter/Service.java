package Concrete;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Service"}, template = "${}")
public class Service {
  private ServiceSupport support = new ServiceSupport();

  public Service decorate() {
    return support.decorate(this);
  }
}
