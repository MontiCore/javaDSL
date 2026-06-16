package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Decorator", template = "${}")
public abstract class Decorator implements Component {
  @Adapt(ref = "Decorator.wrapped", template = "${}")
  protected Component wrapped;

  public Decorator(Component wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  @Adapt(ref = "Component.operation", template = "${}")
  public String operation() {
    return wrapped.operation();
  }
}
