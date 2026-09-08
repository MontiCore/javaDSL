package de.monticore.codeAdaption.evaluation.testcase_12_strategy_singleton_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Singleton"}, template = "${}")
public class Singleton {
    private static Singleton instance;

    @Adapt(ref = {"Singleton.instance"}, template = "get${cap_first}")
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    private Singleton() {
    }
}
