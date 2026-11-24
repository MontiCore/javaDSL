package de.monticore.codeAdaption.cdconcretization.evaluation.paper_examples.task_management.adapter.ticketsystem;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Task"}, template = "${}")
public class Task {
  @Adapt(ref = {"Task.title"}, template = "${}")
  private String title;

  @Adapt(ref = {"Task.description"}, template = "${}")
  private String description;

  @Adapt(ref = {"Task.status"}, template = "${}")
  private TaskStatus status;

}
