package de.monticore.codeAdaption.evaluation.testcase_11_template_method_pattern;

/**
 * CSVProcessor concrete implementation of DataProcessor
 */
public class CSVProcessor extends DataProcessor {
    private String delimiter;

    public CSVProcessor() {
        this.delimiter = ",";
    }

    public CSVProcessor(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() { return delimiter; }

    public void readData() {
        System.out.println("Reading CSV data");
    }

    public void transformData() {
        System.out.println("Transforming CSV data");
    }

    public void validateData() {
        System.out.println("Validating CSV format");
    }
}
