package de.monticore.codeAdaption.matcher.annotMatcher;

import de.monticore.codeAdaption.utils.Adapt;

import java.util.*;
import model.reference.hwc.Application.user.Entity;

@Adapt(
    ref = {"Entity"},
    template = "${}Repository")
public class EntityRepository extends Repository<Entity> {
  @Adapt(
      ref = {"Entity"},
      template = "${uncap_first}Set")
  private Set<Entity> entitySet = new HashSet<>();

  @Adapt(
      ref = {"Entity", "Entity.id"},
      template = "find${}By${cap_first}")
  public Optional<Entity> findEntityById(String id) {
    int i = 0;
    for (Entity element : getAllEntity()) {
      if (element.getId().compareTo(id) == 0) {
        return Optional.of(element);
      }
    }
    return Optional.empty();
  }

  @Adapt(
      ref = {"Entity"},
      template = "getAll${}")
  public Set<Entity> getAllEntity() {
    return entitySet;
  }

  @Adapt(
      ref = {"Entity", "Entity.id"},
      template = "getAll${}SortedBy${cap_first}")
  public List<Entity> getAllEntitySortedById() {

    @Adapt(
        ref = {"Entity"},
        template = "${uncap_first}List")
    List<Entity> entityList = new ArrayList<>(getAllEntity());

    Comparator<Entity> comparator = Comparator.comparing(Entity::getId);

    entityList.sort(comparator);

    return entityList;
  }

  public void store(@Adapt(ref = {"Entity"}, template = "${uncap_first}") Entity entity) {
    entitySet.add(entity);
  }
}
