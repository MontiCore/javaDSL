package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.adapter;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.ArrayList;
import java.util.List;

@Adapt(ref = "Subject", template = "${}")
public class Subject {
  @Adapt(ref = "Subject.observers", template = "${}")
  private final List<Observer> observers = new ArrayList<>();

  @Adapt(ref = "Subject.attach", template = "${}")
  public void attach(Observer observer) {
    observers.add(observer);
  }

  @Adapt(ref = "Subject.detach", template = "${}")
  public void detach(Observer observer) {
    observers.remove(observer);
  }

  @Adapt(ref = "Subject.notifyObservers", template = "${}")
  public void notifyObservers(String event) {
    for (Observer observer : observers) {
      observer.update(event);
    }
  }
}
