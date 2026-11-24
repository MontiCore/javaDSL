package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"StundenzettelEintrag"}, template = "${}")
public class StundenzettelEintrag {
  @Adapt(ref = {"StundenzettelEintrag.uhrzeitVon"}, template = "${}")
  private ZonedDateTime uhrzeitVon;

  @Adapt(ref = {"StundenzettelEintrag.uhrzeitBis"}, template = "${}")
  private Optional<ZonedDateTime> uhrzeitBis;

  @Adapt(ref = {"StundenzettelEintrag.beschreibung"}, template = "${}")
  private Optional<String> beschreibung;

  @Adapt(ref = {"StundenzettelEintrag.stunden"}, template = "${}")
  private long stunden;

  @Adapt(ref = {"StundenzettelEintrag.pauseOderSonstiges"}, template = "${}")
  private StundenzettelProjekt pauseOderSonstiges;

}
