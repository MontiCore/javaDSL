package de.monticore.codeAdaption.evaluation.testcase_9_strategy_pattern;

/**
 * PaymentProcessor domain class
 */
public class PaymentProcessor {
    private String merchantId;

    public PaymentProcessor() {
    }

    public PaymentProcessor(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantId() { return merchantId; }

    public void processPayment(double amount) {
        System.out.println("Processing payment of $" + amount);
    }
}
