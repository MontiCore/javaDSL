package de.monticore.codeAdaption.evaluation.testcase_13_template_observer;

/**
 * MetricsObserver concrete implementation of PipelineObserver
 */
public class MetricsObserver implements PipelineObserver {
    private String metricsEndpoint;

    public MetricsObserver() {
        this.metricsEndpoint = "/metrics";
    }

    public MetricsObserver(String metricsEndpoint) {
        this.metricsEndpoint = metricsEndpoint;
    }

    public String getMetricsEndpoint() { return metricsEndpoint; }

    public void onPipelineStage(String stage) {
        System.out.println("[METRICS] Recording metrics for stage: " + stage);
    }
}
