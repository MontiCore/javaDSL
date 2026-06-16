package de.monticore.codeAdaption.cdconcretization.adapter.evaluation;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Department"}, template = "${}")
public class Department {
  @Adapt(ref = {"Department.name"}, template = "${}")
  private String name;

}
