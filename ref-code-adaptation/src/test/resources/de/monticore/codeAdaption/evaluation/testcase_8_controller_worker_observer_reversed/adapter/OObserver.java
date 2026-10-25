package de.monticore.codeAdaption.evaluation.testcase_8_controller_worker_observer_reversed;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Observer"}, template = "${}")
public interface OObserver {

    @Adapt(ref = {"Observer.update"}, template = "update")
    void update();
}




