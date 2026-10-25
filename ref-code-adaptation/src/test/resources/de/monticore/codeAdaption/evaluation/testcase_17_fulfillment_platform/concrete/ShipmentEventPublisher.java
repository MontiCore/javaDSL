package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

/** The observer field and publication loop are supplied by adapted reference code. */
public class ShipmentEventPublisher {
  public void subscribe(ShipmentEventListener listener) {
    listeners.add(listener);
  }

  public void unsubscribe(ShipmentEventListener listener) {
    listeners.remove(listener);
  }
}
