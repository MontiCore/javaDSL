package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Mittelzuweisung"}, template = "${}")
public class Mittelzuweisung extends Buchungseintrag {
  @Adapt(ref = {"Mittelzuweisung.verfallDatum"}, template = "${}")
  private Optional<ZonedDateTime> verfallDatum;

  @Adapt(ref = {"Mittelzuweisung.kennung"}, template = "${}")
  private Optional<String> kennung;

  @Adapt(ref = {"Mittelzuweisung.status"}, template = "${}")
  private MittelzuweisungStatus status;

}
