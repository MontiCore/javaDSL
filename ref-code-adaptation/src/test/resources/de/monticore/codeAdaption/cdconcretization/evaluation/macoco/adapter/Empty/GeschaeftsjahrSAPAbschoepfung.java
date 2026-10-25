package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"GeschaeftsjahrSAPAbschoepfung"}, template = "${}")
public class GeschaeftsjahrSAPAbschoepfung {
  @Adapt(ref = {"GeschaeftsjahrSAPAbschoepfung.fachgruppe"}, template = "${}")
  private String fachgruppe;

  @Adapt(ref = {"GeschaeftsjahrSAPAbschoepfung.institutsKennZiffer"}, template = "${}")
  private String institutsKennZiffer;

  @Adapt(ref = {"GeschaeftsjahrSAPAbschoepfung.institutsName"}, template = "${}")
  private String institutsName;

  @Adapt(ref = {"GeschaeftsjahrSAPAbschoepfung.pspElement"}, template = "${}")
  private String pspElement;

  @Adapt(ref = {"GeschaeftsjahrSAPAbschoepfung.gesamtSAPAbschoepfung"}, template = "${}")
  private long gesamtSAPAbschoepfung;

}
