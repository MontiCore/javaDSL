package de.monticore.codeAdaption.evaluation.reference.repository;

import de.monticore.codeAdaption.evaluation.reference.entity.Role;
import de.monticore.codeAdaption.evaluation.reference.entity.User;

import java.util.List;

public interface RoleRepository  extends  CRUDRepository<String>{
    User getRoleByName(String name);

    void storeRole(User user);

    List<Role> getAllRole();

    List<Role> getAllRoleSortedByName();

    void  removeRole();
}
