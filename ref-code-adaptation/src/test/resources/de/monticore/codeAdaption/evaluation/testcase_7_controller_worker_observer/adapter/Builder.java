package de.monticore.codeAdaption.evaluation.testcase_7_controller_worker_observer;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Builder"}, template = "${}", genTemplate = "${}Builder")
public class Builder {
    private Object attribute;

    @Adapt(ref = {"Builder.attribute"}, template = "set${cap_first}")
    public Builder setAttribute(Object attribute) {
        this.attribute = attribute;
        return this;
    }
}

