package de.monticore.codeAdaption.evaluation.reference.builders;

import de.monticore.codeAdaption.evaluation.reference.entity.Role;
import de.monticore.codeAdaption.utils.Adapt;

public class RoleBuilder {
  private String name;

  public String getName() {
    return name;
  }

  public RoleBuilder setName(String name) {
    this.name = name;
    return this;
  }

  @Adapt(ignore = true)
  public Role build() {
    Role role = new Role();
    role.setName(name);
    return role;
  }
}
