package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton;

/**
 * DatabaseStorageStrategy concrete implementation
 */
public class DatabaseStorageStrategy implements ConfigStorageStrategy {
    private String connectionString;

    public DatabaseStorageStrategy() {
        this.connectionString = "jdbc:default";
    }

    public DatabaseStorageStrategy(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getConnectionString() { return connectionString; }

    public void save(String key, String value) {
        System.out.println("Saving to database: " + key + " = " + value);
    }

    public String load(String key) {
        System.out.println("Loading from database: " + key);
        return "db_value";
    }
}
