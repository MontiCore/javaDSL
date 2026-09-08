package de.monticore.codeAdaption.cdconcretization.evaluation.macoco.adapter.empty;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"PSPImportFilter"}, template = "${}")
public class PSPImportFilter {
  @Adapt(ref = {"PSPImportFilter.importThisPSPelement"}, template = "${}")
  private boolean importThisPSPelement;

  @Adapt(ref = {"PSPImportFilter.importBookings"}, template = "${}")
  private boolean importBookings;

  @Adapt(ref = {"PSPImportFilter.name"}, template = "${}")
  private String name;

  @Adapt(ref = {"PSPImportFilter.pspElement"}, template = "${}")
  private String pspElement;

  @Adapt(ref = {"PSPImportFilter.startDate"}, template = "${}")
  private Optional<ZonedDateTime> startDate;

}
