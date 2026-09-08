package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton;

/**
 * FileStorageStrategy concrete implementation
 */
public class FileStorageStrategy implements ConfigStorageStrategy {
    private String basePath;

    public FileStorageStrategy() {
        this.basePath = "/config";
    }

    public FileStorageStrategy(String basePath) {
        this.basePath = basePath;
    }

    public String getBasePath() { return basePath; }

    public void save(String key, String value) {
        System.out.println("Saving to file: " + key + " = " + value);
    }

    public String load(String key) {
        System.out.println("Loading from file: " + key);
        return "file_value";
    }
}
