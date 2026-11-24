package de.monticore.codeAdaption.evaluation.testcase_8_controller_worker_observer_reversed;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.List;

@Adapt(ref = {"Subject"}, template = "${}")
public class OSubject {

    @Adapt(ref = {"Subject.observers"}, template = "${}")
    private List<Observer> observers;

    @Adapt(ref = {"Subject.subscribe"}, template = "subscribe")
    public boolean subscribe(Observer w) {
        // template method body - adapter will replace with concrete implementation
        return observers != null && observers.add(w);
    }

    @Adapt(ref = {"Subject.unsubscribe"}, template = "unsubscribe")
    public boolean unsubscribe(Observer w) {
        return observers != null && observers.remove(w);
    }

    @Adapt(ref = {"Subject.notifyAll"}, template = "notifyAll")
    public void notifyAll() throws Exception {
        if (observers == null) return;
        for (Observer w : observers) {
            w.update();
        }
    }
}



