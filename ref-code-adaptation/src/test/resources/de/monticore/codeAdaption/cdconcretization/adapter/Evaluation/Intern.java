package de.monticore.codeAdaption.cdconcretization.adapter.evaluation;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Intern"}, template = "${}")
public class Intern extends Person {
  @Adapt(ref = {"Intern.supervisorID"}, template = "${}")
  private int supervisorID;

}
