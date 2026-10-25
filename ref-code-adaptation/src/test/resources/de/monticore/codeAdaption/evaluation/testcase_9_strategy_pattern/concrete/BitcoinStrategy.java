package de.monticore.codeAdaption.evaluation.testcase_9_strategy_pattern;

/**
 * BitcoinStrategy concrete implementation of PaymentStrategy
 */
public class BitcoinStrategy implements PaymentStrategy {
    private String walletAddress;

    public BitcoinStrategy() {
    }

    public BitcoinStrategy(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getWalletAddress() { return walletAddress; }

    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using Bitcoin");
    }
}
