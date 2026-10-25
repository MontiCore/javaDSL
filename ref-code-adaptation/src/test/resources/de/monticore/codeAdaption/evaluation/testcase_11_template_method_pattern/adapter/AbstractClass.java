package de.monticore.codeAdaption.evaluation.testcase_11_template_method_pattern;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"AbstractClass"}, template = "${}")
public abstract class AbstractClass {

    @Adapt(ref = {"AbstractClass.templateMethod"}, template = "${}")
    public void templateMethod() {
        primitiveOperation1();
        primitiveOperation2();
        hook();
    }

    @Adapt(ref = {"AbstractClass.primitiveOperation1"}, template = "${}")
    public abstract void primitiveOperation1();

    @Adapt(ref = {"AbstractClass.primitiveOperation2"}, template = "${}")
    public abstract void primitiveOperation2();

    @Adapt(ref = {"AbstractClass.hook"}, template = "${}")
    public void hook() {
        // Default implementation
        System.out.println("Hook");
    }
}
