package de.monticore.codeAdaption.cdconcretization.evaluation.paper_examples.task_management.adapter.ticketsystem;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Member"}, template = "${}")
public class Member {
  @Adapt(ref = {"Member.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"Member.email"}, template = "${}")
  private String email;

}
