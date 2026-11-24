package de.monticore.codeAdaption.evaluation.testcase_13_template_observer_pattern;

import de.monticore.codeAdaption.utils.Adapt;
import java.util.List;
import java.util.ArrayList;

@Adapt(ref = {"Subject"}, template = "${}")
public class Subject {
    private List<Observer> observers = new ArrayList<>();

    @Adapt(ref = {"Subject.subscribe"}, template = "subscribe")
    public boolean subscribe(Observer o) {
        return observers.add(o);
    }

    @Adapt(ref = {"Subject.unsubscribe"}, template = "unsubscribe")
    public boolean unsubscribe(Observer o) {
        return observers.remove(o);
    }

    @Adapt(ref = {"Subject.notifyAll"}, template = "notifyAll")
    public void notifyAll() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
