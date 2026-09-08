package de.monticore.codeAdaption.evaluation.testcase_11_template_method_pattern;

/**
 * XMLProcessor concrete implementation of DataProcessor
 */
public class XMLProcessor extends DataProcessor {
    private String encoding;

    public XMLProcessor() {
        this.encoding = "UTF-8";
    }

    public XMLProcessor(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() { return encoding; }

    public void readData() {
        System.out.println("Reading XML data");
    }

    public void transformData() {
        System.out.println("Transforming XML data");
    }

    public void validateData() {
        System.out.println("Validating XML DTD");
    }
}
