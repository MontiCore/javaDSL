package de.monticore.codeAdaption.evaluation.testcase_13_template_observer;

/**
 * SalesDataPipeline concrete implementation of DataPipeline
 */
public class SalesDataPipeline extends DataPipeline {
    private String crmSystem;

    public SalesDataPipeline() {
        this.crmSystem = "Salesforce";
    }

    public SalesDataPipeline(String crmSystem) {
        this.crmSystem = crmSystem;
    }

    public String getCrmSystem() { return crmSystem; }

    public void extractData() {
        System.out.println("Extracting sales data from CRM");
    }

    public void processData() {
        System.out.println("Processing sales data with business rules");
    }

    public void loadData() {
        System.out.println("Loading processed sales data to warehouse");
    }
}
