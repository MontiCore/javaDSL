package de.monticore.codeAdaption.evaluation.testcase_7_controller_worker_observer;

/**
 * Controller domain class used in the controller/worker observer example
 */
public class Controller {
    private String id;
    private String status;

    public Controller() {
    }

    public Controller(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }

    public void sX(Object x) { }
}



