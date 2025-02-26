package de.monticore.codeAdaption.matcher.annotMatcher;

import java.util.*;
import model.reference.hwc.Application.user.Entity;

import javax.swing.text.html.parser.Entity;


public class EntityRepository extends Repository<Entity> {

  private Set<Entity> entitySet = new HashSet<>();


  public Optional<Entity> findEntityById(String id) {
    return Optional.empty();
  }

  public Set<Entity> getAllEntity() {
    return entitySet;
  }

  public List<Entity> getAllEntitySortedById() {
    List<Entity> entityList = new ArrayList<>(getAllEntity());
  }

  public void storeEntity(Entity entity){

  }

}
