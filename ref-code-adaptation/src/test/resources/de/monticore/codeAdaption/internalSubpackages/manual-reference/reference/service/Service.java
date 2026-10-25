package reference.service;

import reference.model.Entity;
import de.monticore.codeAdaption.utils.Adapt;

public class Service {
  @Adapt(ref = "Service.echo", template = "${}")
  public Entity echo(Entity entity) {
    return entity;
  }

  public int value() {
    Entity entity = new Entity();
    entity.id = 7;
    return echo(entity).id;
  }
}
