package de.monticore.codeAdaption.cdconcretization.adapter.evaluation;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Person"}, template = "${}")
public class Person {
  @Adapt(ref = {"Person.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"Person.age"}, template = "${}")
  private int age;

}
