package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"F1Konto"}, template = "${}")
public class F1Konto extends Haushaltskonto {
  @Adapt(ref = {"F1Konto.startDatum"}, template = "${}")
  private Optional<ZonedDateTime> startDatum;

  @Adapt(ref = {"F1Konto.endDatum"}, template = "${}")
  private Optional<ZonedDateTime> endDatum;

  @Adapt(ref = {"F1Konto.originalBudgetCent"}, template = "${}")
  private long originalBudgetCent;

  @Adapt(ref = {"F1Konto.sonstigeZuweisungenCent"}, template = "${}")
  private long sonstigeZuweisungenCent;

  @Adapt(ref = {"F1Konto.resteCent"}, template = "${}")
  private long resteCent;

  @Adapt(ref = {"F1Konto.aktuellerKontostandCent"}, template = "${}")
  private long aktuellerKontostandCent;

  @Adapt(ref = {"F1Konto.kontoRahmenCent"}, template = "${}")
  private long kontoRahmenCent;

  @Adapt(ref = {"F1Konto.kommunikationsStatus"}, template = "${}")
  private KommunikationsStatus kommunikationsStatus;

  @Adapt(ref = {"F1Konto.strafsteuerStatus"}, template = "${}")
  private StrafsteuerStatus strafsteuerStatus;

  @Adapt(ref = {"F1Konto.strafsteuerBasisCent"}, template = "${}")
  private long strafsteuerBasisCent;

  @Adapt(ref = {"F1Konto.strafsteuerSAPCent"}, template = "${}")
  private long strafsteuerSAPCent;

  @Adapt(ref = {"F1Konto.strafsteuerCent"}, template = "${}")
  private long strafsteuerCent;

  @Adapt(ref = {"F1Konto.strafsteuerBerechnungsGrundlageCent"}, template = "${}")
  private long strafsteuerBerechnungsGrundlageCent;

  @Adapt(ref = {"F1Konto.kontoStandBeginnGeschaeftsjahr"}, template = "${}")
  private Optional<Long> kontoStandBeginnGeschaeftsjahr;

}
