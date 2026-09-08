package de.monticore.codeAdaption.evaluation.testcase_13_template_observer_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"AbstractClass"}, template = "${}")
public abstract class AbstractClass {

    @Adapt(ref = {"AbstractClass.templateMethod"}, template = "templateMethod")
    public void templateMethod() {
        primitiveOperation1();
        primitiveOperation2();
        hook();
    }

    @Adapt(ref = {"AbstractClass.primitiveOperation1"}, template = "primitiveOperation1")
    public abstract void primitiveOperation1();

    @Adapt(ref = {"AbstractClass.primitiveOperation2"}, template = "primitiveOperation2")
    public abstract void primitiveOperation2();

    @Adapt(ref = {"AbstractClass.hook"}, template = "hook")
    public void hook() {
        // Default implementation
    }
}
