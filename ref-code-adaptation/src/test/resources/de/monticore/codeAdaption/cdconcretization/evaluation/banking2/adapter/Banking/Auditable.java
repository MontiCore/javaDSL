package de.monticore.codeAdaption.cdconcretization.evaluation.banking2.adapter.banking;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Auditable"}, template = "${}")
public interface Auditable {
  @Adapt(ref = {"Auditable.generateReport"}, template = "${}")
  String generateReport();

}
