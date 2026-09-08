package de.monticore.codeAdaption.cdconcretization.evaluation.observer;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Subject"}, template = "${}")
public class Subject {
  @Adapt(ref = {"Subject.register"}, template = "${}")
  public void register(Observer o) {
  }

  @Adapt(ref = {"Subject.unregister"}, template = "${}")
  public void unregister(Observer o) {
  }

  @Adapt(ref = {"Subject.notifyObservers"}, template = "${}")
  public void notifyObservers() {
  }

}
