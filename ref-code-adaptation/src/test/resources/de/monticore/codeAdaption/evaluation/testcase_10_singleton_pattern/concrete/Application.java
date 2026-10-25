package de.monticore.codeAdaption.evaluation.testcase_10_singleton_pattern;

/**
 * Application domain class
 */
public class Application {
    private DatabaseConnection db;
    private Logger logger;

    public Application() {
        this.db = new DatabaseConnection();
        this.logger = new Logger();
    }

    public Application(DatabaseConnection db, Logger logger) {
        this.db = db;
        this.logger = logger;
    }

    public void initialize() {
        db.connect();
        logger.log("Application initialized");
    }

    public DatabaseConnection getDb() { return db; }
    public Logger getLogger() { return logger; }
}
