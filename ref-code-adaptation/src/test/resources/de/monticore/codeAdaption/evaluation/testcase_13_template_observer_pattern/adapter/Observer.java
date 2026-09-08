package de.monticore.codeAdaption.evaluation.testcase_13_template_observer_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Observer"}, template = "${}")
public interface Observer {

    @Adapt(ref = {"Observer.update"}, template = "update")
    void update();
}
