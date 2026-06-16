package de.monticore.codeAdaption.cdconcretization.inheritance;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Person"}, template = "${}")
public class Person {
  @Adapt(ref = {"Person.number"}, template = "${}")
  private int number;

}
