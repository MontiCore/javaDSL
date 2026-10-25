package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class CustomerNotificationListener implements ShipmentEventListener {
  private final CustomerNotifier notifier;
  private final String customerEmail;

  public CustomerNotificationListener(CustomerNotifier notifier, String customerEmail) {
    this.notifier = notifier;
    this.customerEmail = customerEmail;
  }

  @Override
  public void onShipmentEvent(ShipmentRecord shipment) {
    notifier.notifyCustomer(
        customerEmail, "Shipment " + shipment.getLabel() + " is booked via " + shipment.getRoute());
  }
}
