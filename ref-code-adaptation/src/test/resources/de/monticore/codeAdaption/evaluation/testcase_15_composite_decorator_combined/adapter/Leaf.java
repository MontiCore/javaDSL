package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Leaf", template = "${}")
public class Leaf implements Component {
  @Override
  @Adapt(ref = "Component.operation", template = "${}")
  public String operation() {
    return "";
  }
}
