package de.monticore.codeAdaption.evaluation.testcase_9_strategy_pattern;

/**
 * PayPalStrategy concrete implementation of PaymentStrategy
 */
public class PayPalStrategy implements PaymentStrategy {
    private String email;

    public PayPalStrategy() {
    }

    public PayPalStrategy(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }

    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using PayPal");
    }
}
