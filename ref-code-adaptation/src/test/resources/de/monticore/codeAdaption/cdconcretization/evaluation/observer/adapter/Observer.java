package de.monticore.codeAdaption.cdconcretization.evaluation.observer;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Observer"}, template = "${}")
public class Observer {
  @Adapt(ref = {"Observer.update"}, template = "${}")
  public void update() {
  }

}
