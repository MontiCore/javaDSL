package de.monticore.codeAdaption.cdconcretization.evaluation.cross_references.adapter.microservice;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"Microservice"}, template = "${}")
public class Microservice {
  @Adapt(ref = {"Microservice.sendToMicroservice"}, template = "${}")
  public void sendToMicroservice(String message) {
  }

}
