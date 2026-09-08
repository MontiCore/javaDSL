package de.monticore.codeAdaption.evaluation.testcase_11_template_method_pattern;

/**
 * JSONProcessor concrete implementation of DataProcessor
 */
public class JSONProcessor extends DataProcessor {
    private boolean prettyPrint;

    public JSONProcessor() {
        this.prettyPrint = false;
    }

    public JSONProcessor(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public boolean isPrettyPrint() { return prettyPrint; }

    public void readData() {
        System.out.println("Reading JSON data");
    }

    public void transformData() {
        System.out.println("Transforming JSON data");
    }

    public void validateData() {
        System.out.println("Validating JSON schema");
    }
}
