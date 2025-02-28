import de.monticore.codeAdaption.utils.Adapt;
@Adapt(ref = "Entity",template = "${}")
public class Entity {
  @Adapt(ref = "Entity.id",template = "${}")
  String id;
}
