package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton;

/**
 * ConfigStorageStrategy interface
 */
public interface ConfigStorageStrategy {
    void save(String key, String value);
    String load(String key);
}
