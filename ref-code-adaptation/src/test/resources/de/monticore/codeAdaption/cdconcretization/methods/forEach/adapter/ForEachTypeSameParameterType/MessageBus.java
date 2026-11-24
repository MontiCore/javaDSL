package de.monticore.codeAdaption.cdconcretization.methods.foreach.adapter.foreachtypesameparametertype;

import de.monticore.codeAdaption.utils.Adapt;

@Adapt(ref = {"MessageBus"}, template = "${}")
public class MessageBus {
  @Adapt(ref = {"MessageBus.sendMessage"}, template = "${}")
  public void sendMessage(Message message) {
  }

}
