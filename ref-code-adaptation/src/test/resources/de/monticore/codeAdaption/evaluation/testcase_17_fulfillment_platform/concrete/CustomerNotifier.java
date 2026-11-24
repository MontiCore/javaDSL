package de.monticore.codeAdaption.evaluation.testcase_17_fulfillment_platform.concrete;

public class CustomerNotifier {
  private String latestRecipient = "";
  private String latestMessage = "";

  public void notifyCustomer(String email, String message) {
    latestRecipient = email;
    latestMessage = message;
  }

  public boolean notified(String email) {
    return latestRecipient.equals(email) && !latestMessage.isBlank();
  }
}
