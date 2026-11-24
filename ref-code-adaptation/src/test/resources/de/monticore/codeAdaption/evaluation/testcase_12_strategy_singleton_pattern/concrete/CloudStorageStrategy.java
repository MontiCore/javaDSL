package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton;

/**
 * CloudStorageStrategy concrete implementation
 */
public class CloudStorageStrategy implements ConfigStorageStrategy {
    private String bucketName;

    public CloudStorageStrategy() {
        this.bucketName = "default-bucket";
    }

    public CloudStorageStrategy(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() { return bucketName; }

    public void save(String key, String value) {
        System.out.println("Saving to cloud: " + key + " = " + value);
    }

    public String load(String key) {
        System.out.println("Loading from cloud: " + key);
        return "cloud_value";
    }
}
