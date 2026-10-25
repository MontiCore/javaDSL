package de.monticore.codeAdaption.evaluation.testcase_9_strategy_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Strategy"}, template = "${}")
public interface Strategy {

    @Adapt(ref = {"Strategy.execute"}, template = "${}")
    void execute();
}
