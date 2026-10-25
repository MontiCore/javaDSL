package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Strategy"}, template = "${}")
public interface Strategy {

    @Adapt(ref = {"Strategy.execute"}, template = "execute")
    void execute();
}
