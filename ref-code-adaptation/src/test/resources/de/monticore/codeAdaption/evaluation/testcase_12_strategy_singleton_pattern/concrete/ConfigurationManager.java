package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton;

/**
 * ConfigurationManager domain class
 */
public class ConfigurationManager {
    private String environment;
    private ConfigStorageStrategy storageStrategy;

    public ConfigurationManager() {
        this.environment = "production";
        this.storageStrategy = new FileStorageStrategy();
    }

    public ConfigurationManager(String environment, ConfigStorageStrategy storageStrategy) {
        this.environment = environment;
        this.storageStrategy = storageStrategy;
    }

    public String getEnvironment() { return environment; }
    public ConfigStorageStrategy getStorageStrategy() { return storageStrategy; }

    public void setStorageStrategy(ConfigStorageStrategy strategy) {
        this.storageStrategy = strategy;
    }

    public void saveConfig(String key, String value) {
        storageStrategy.save(key, value);
    }

    public String loadConfig(String key) {
        return storageStrategy.load(key);
    }
}
