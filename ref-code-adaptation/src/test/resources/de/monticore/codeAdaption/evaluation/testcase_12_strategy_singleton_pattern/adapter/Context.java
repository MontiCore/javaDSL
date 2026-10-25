package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Context"}, template = "${}")
public class Context {
    private Strategy strategy;

    @Adapt(ref = {"Context.strategy"}, template = "set${cap_first}")
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    @Adapt(ref = {"Context.execute"}, template = "execute")
    public void execute() {
        strategy.execute();
    }
}
