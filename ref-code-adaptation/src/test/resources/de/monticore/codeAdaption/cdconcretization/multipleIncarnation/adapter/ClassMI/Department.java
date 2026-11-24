package de.monticore.codeAdaption.cdconcretization.multipleincarnation.adapter.classmi;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Department"}, template = "${}")
public class Department {
  @Adapt(ref = {"Department.deptID"}, template = "${}")
  private int deptID;

}
