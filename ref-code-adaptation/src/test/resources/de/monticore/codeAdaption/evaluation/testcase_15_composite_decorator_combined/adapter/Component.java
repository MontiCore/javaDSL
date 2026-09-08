package de.monticore.codeAdaption.evaluation.testcase_15_composite_decorator_combined.adapter;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = "Component", template = "${}")
public interface Component {
  @Adapt(ref = "Component.operation", template = "${}")
  String operation();
}
