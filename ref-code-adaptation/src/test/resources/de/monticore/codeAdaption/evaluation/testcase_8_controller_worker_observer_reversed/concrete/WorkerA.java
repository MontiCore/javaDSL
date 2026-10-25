package de.monticore.codeAdaption.evaluation.testcase_8_controller_worker_observer_reversed;

/**
 * WorkerA domain class (observer implementation)
 */
public class WorkerA implements WorkerInterface {
    private String name;

    public WorkerA() {}

    public WorkerA(String name) { this.name = name; }

    public String getName() { return name; }

    public void update() { /* perform update handling */ }

    @Override
    public void work() { /* perform work - adapter may map observer calls to this */ }

    public void sX(Object x) { }
    
    
}




