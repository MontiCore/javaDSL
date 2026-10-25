package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Stundenzettel"}, template = "${}")
public class Stundenzettel {
  @Adapt(ref = {"Stundenzettel.status"}, template = "${}")
  private StundenzettelStatus status;

  @Adapt(ref = {"Stundenzettel.zeit"}, template = "${}")
  private ZonedDateTime zeit;

  @Adapt(ref = {"Stundenzettel.abgegebenVonUserId"}, template = "${}")
  private Optional<Long> abgegebenVonUserId;

  @Adapt(ref = {"Stundenzettel.abgabeDatum"}, template = "${}")
  private Optional<ZonedDateTime> abgabeDatum;

}
