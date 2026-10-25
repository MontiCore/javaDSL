package de.monticore.codeAdaption.cdconcretization.multipleincarnation.adapter.attributetypemi;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Employee"}, template = "${}")
public class Employee {
  @Adapt(ref = {"Employee.value"}, template = "${}")
  private SomeValueType value;

}
