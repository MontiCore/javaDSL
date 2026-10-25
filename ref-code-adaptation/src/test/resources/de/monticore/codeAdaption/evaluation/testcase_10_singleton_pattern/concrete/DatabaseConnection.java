package de.monticore.codeAdaption.evaluation.testcase_10_singleton_pattern;

/**
 * DatabaseConnection domain class
 */
public class DatabaseConnection {
    private String connectionString;
    private boolean isConnected;

    public DatabaseConnection() {
        this.connectionString = "jdbc:default";
        this.isConnected = false;
    }

    public DatabaseConnection(String connectionString) {
        this.connectionString = connectionString;
        this.isConnected = false;
    }

    public String getConnectionString() { return connectionString; }
    public boolean isConnected() { return isConnected; }

    public void connect() {
        this.isConnected = true;
    }

    public void disconnect() {
        this.isConnected = false;
    }
}
