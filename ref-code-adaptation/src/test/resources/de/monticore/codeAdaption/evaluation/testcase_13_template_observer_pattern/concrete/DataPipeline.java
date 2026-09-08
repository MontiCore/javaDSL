package de.monticore.codeAdaption.evaluation.testcase_13_template_observer;

/**
 * DataPipeline abstract class
 */
public abstract class DataPipeline {
    private String pipelineName;
    private String source;

    public DataPipeline() {
        this.pipelineName = "default";
        this.source = "default";
    }

    public DataPipeline(String pipelineName, String source) {
        this.pipelineName = pipelineName;
        this.source = source;
    }

    public String getPipelineName() { return pipelineName; }
    public String getSource() { return source; }

    public abstract void extractData();
    public abstract void processData();
    public abstract void loadData();
}
