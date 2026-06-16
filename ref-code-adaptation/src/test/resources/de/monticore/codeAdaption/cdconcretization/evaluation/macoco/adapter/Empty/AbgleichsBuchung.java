package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"AbgleichsBuchung"}, template = "${}")
public class AbgleichsBuchung extends Buchung {
  @Adapt(ref = {"AbgleichsBuchung.abgleichsStatus"}, template = "${}")
  private AbgleichsStatus abgleichsStatus;

  @Adapt(ref = {"AbgleichsBuchung.abgleichKommentar"}, template = "${}")
  private Optional<String> abgleichKommentar;

  @Adapt(ref = {"AbgleichsBuchung.positionsNummer"}, template = "${}")
  private String positionsNummer;

  @Adapt(ref = {"AbgleichsBuchung.deactivated"}, template = "${}")
  private boolean deactivated;

}
