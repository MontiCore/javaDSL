package de.monticore.codeAdaption.cdconcretization.inheritance.adapter.attributeinheritance;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Person"}, template = "${}")
public class Person {
  @Adapt(ref = {"Person.number"}, template = "${}")
  private int number;

}
