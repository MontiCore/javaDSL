package de.monticore.codeAdaption.evaluation.testcase_13_template_observer;

/**
 * LoggingObserver concrete implementation of PipelineObserver
 */
public class LoggingObserver implements PipelineObserver {
    private String logLevel;

    public LoggingObserver() {
        this.logLevel = "INFO";
    }

    public LoggingObserver(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogLevel() { return logLevel; }

    public void onPipelineStage(String stage) {
        System.out.println("[LOG] Pipeline stage: " + stage);
    }
}
