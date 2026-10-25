package reference.service;

import reference.model.Entity;
import reference.service.internal.EntitySupport;

public class Service {
  public int value() {
    Entity entity = new Entity();
    entity.id = 7;
    return EntitySupport.echo(entity).id;
  }
}
