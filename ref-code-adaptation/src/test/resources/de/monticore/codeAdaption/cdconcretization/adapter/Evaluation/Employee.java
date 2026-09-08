package de.monticore.codeAdaption.cdconcretization.adapter.evaluation;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Employee"}, template = "${}")
public class Employee extends Person {
  @Adapt(ref = {"Employee.id"}, template = "${}")
  private int id;

}
