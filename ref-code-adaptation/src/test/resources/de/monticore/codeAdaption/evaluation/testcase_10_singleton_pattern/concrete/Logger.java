package de.monticore.codeAdaption.evaluation.testcase_10_singleton_pattern;

/**
 * Logger domain class
 */
public class Logger {
    private String logFile;

    public Logger() {
        this.logFile = "application.log";
    }

    public Logger(String logFile) {
        this.logFile = logFile;
    }

    public String getLogFile() { return logFile; }

    public void log(String message) {
        System.out.println("LOG: " + message);
    }
}
