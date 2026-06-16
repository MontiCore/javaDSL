package de.monticore.codeAdaption.evaluation.testcase_6_builder_pattern;

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