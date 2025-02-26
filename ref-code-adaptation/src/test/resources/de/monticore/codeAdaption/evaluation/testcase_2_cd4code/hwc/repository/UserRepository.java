package de.monticore.codeAdaption.evaluation.testcase_2_cd4code.Concrete;

import de.monticore.codeAdaption.evaluation.reference.entity.User;

import java.util.List;

public interface UserRepository extends CRUDRepository<String> {
  User getUserByName(String name);

  void storeUser(User user);

  List<User> getAllUser();

  List<User> getAllUserSortedByName();

  void  removeUser();
}
