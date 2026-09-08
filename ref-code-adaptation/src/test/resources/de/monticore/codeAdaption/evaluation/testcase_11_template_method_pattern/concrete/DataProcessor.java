package de.monticore.codeAdaption.evaluation.testcase_11_template_method_pattern;

/**
 * DataProcessor abstract class
 */
public abstract class DataProcessor {
    private String source;
    private String destination;

    public DataProcessor() {
        this.source = "default";
        this.destination = "default";
    }

    public DataProcessor(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() { return source; }
    public String getDestination() { return destination; }

    public abstract void readData();
    public abstract void transformData();

    public void validateData() {
        System.out.println("Validating data...");
    }
}
