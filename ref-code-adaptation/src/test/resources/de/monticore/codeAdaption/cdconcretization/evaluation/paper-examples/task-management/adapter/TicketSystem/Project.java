package de.monticore.codeAdaption.cdconcretization.evaluation.paper_examples.task_management.adapter.ticketsystem;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Project"}, template = "${}")
public class Project {
  @Adapt(ref = {"Project.name"}, template = "${}")
  private String name;

}
