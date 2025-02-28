package de.monticore.codeAdaption.evaluation.reference.repository;

import de.monticore.codeAdaption.evaluation.reference.entity.User;

import java.util.List;

public interface UserRepository extends CRUDRepository<String> {
  User getUserByLoginName(String name);

  void storeUser(User user);

  List<User> getAllUser();

  List<User> getAllUserSortedByLoginName();

  void  removeUser();
}
