package de.monticore.codeAdaption.evaluation.testcase_9_strategy_pattern;

/**
 * CreditCardStrategy concrete implementation of PaymentStrategy
 */
public class CreditCardStrategy implements PaymentStrategy {
    private String cardNumber;

    public CreditCardStrategy() {
    }

    public CreditCardStrategy(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardNumber() { return cardNumber; }

    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Credit Card");
    }
}
