
import Repository;
import de.monticore.codeAdaption.utils.Adapt;

import java.util.*;

import Entity;

@Adapt(ref = {"Entity"}, template = "${}Repository")
public class EntityRepository extends Repository<Entity> {

    @Adapt(ref = {"Entity"}, template = "${uncap_first}Set")
    private Set<Entity> entitySet = new HashSet<>();

    @Adapt(ref = {"Entity", "Entity.id"}, template = "find${}By${cap_first}")
    public Optional<Entity> findEntityById(String id) {
      return Optional.empty();
    }

    @Adapt(ref = {"Entity", "Entity.id"}, template = "getAll${}SortedBy${cap_first}")
    public List<Entity> getAllEntitySortedById() {
        @Adapt(ref = {"Entity"}, template = "${uncap_first}List")
        List<Entity> entityList = new ArrayList<>();
    }
   @Adapt(ignore = true)
     public void store(
            @Adapt(ref = {"Entity"}, template = "${uncap_first}") Entity entity) {
        entitySet.add(entity);
    }
}
