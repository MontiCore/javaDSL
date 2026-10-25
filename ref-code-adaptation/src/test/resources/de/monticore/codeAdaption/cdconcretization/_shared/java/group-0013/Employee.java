package de.monticore.codeAdaption.cdconcretization.attributes.valid.adapter.attributeindeepsuperclass;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Employee"}, template = "${}")
public class Employee {
  @Adapt(ref = {"Employee.firstName"}, template = "${}")
  private String firstName;

  @Adapt(ref = {"Employee.lastName"}, template = "${}")
  private String lastName;

  @Adapt(ref = {"Employee.number"}, template = "${}")
  private int number;

  @Adapt(ref = {"Employee.salary"}, template = "${}")
  private int salary;

}
