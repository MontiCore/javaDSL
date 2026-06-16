package de.monticore.codeAdaption.evaluation.testcase_13_template_observer;

/**
 * InventoryDataPipeline concrete implementation of DataPipeline
 */
public class InventoryDataPipeline extends DataPipeline {
    private String erpSystem;

    public InventoryDataPipeline() {
        this.erpSystem = "SAP";
    }

    public InventoryDataPipeline(String erpSystem) {
        this.erpSystem = erpSystem;
    }

    public String getErpSystem() { return erpSystem; }

    public void extractData() {
        System.out.println("Extracting inventory data from ERP");
    }

    public void processData() {
        System.out.println("Processing inventory data with stock rules");
    }

    public void loadData() {
        System.out.println("Loading processed inventory data to warehouse");
    }
}
