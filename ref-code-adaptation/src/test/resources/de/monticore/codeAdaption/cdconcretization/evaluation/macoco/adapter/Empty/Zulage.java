package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Zulage"}, template = "${}")
public class Zulage {
  @Adapt(ref = {"Zulage.pspelement"}, template = "${}")
  private Optional<String> pspelement;

  @Adapt(ref = {"Zulage.bezeichnung"}, template = "${}")
  private Optional<String> bezeichnung;

  @Adapt(ref = {"Zulage.von"}, template = "${}")
  private ZonedDateTime von;

  @Adapt(ref = {"Zulage.bis"}, template = "${}")
  private ZonedDateTime bis;

  @Adapt(ref = {"Zulage.arbeitgeberbrutto"}, template = "${}")
  private Long arbeitgeberbrutto;

  @Adapt(ref = {"Zulage.arbeitnehmerbrutto"}, template = "${}")
  private Long arbeitnehmerbrutto;

}
