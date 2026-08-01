package Concrete;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Service"}, template = "${}")
public class Service {
  private final ServiceSupport support = new ServiceSupport();

  public Service normalize() {
    return support.normalize(this);
  }
}
