package de.monticore.codeAdaption.evaluation.testcase_2_cd4code.Concrete;

import de.monticore.codeAdaption.utils.Adapt;

public class User extends UserTOP {

  @Adapt(ref = "Role", template = "print${}s")
  public String printRoles() {
    String roleNames = "";
    for (Role role : this.roles){
      roleNames = role.name + ", ";
    }
    return roleNames;
  }
}
