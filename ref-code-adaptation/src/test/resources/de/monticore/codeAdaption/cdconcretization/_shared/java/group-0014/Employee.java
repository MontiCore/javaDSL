package de.monticore.codeAdaption.cdconcretization.attributes.valid.adapter.attributesmissing;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Employee"}, template = "${}")
public class Employee {
  @Adapt(ref = {"Employee.number"}, template = "${}")
  private int number;

  @Adapt(ref = {"Employee.salary"}, template = "${}")
  private int salary;

}
